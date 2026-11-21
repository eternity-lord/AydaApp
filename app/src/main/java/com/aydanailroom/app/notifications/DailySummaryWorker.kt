package com.aydanailroom.app.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.aydanailroom.app.MainActivity
import com.aydanailroom.app.R
import com.aydanailroom.app.data.AppDatabase
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class DailySummaryWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        return runCatching {
            NotificationChannels.create(applicationContext)
            val zone = ZoneId.systemDefault()
            val now = LocalDate.now(zone)
            val from = now.atStartOfDay(zone).toInstant().toEpochMilli()
            val until = now.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val count = AppDatabase.get(applicationContext).appDao().countAppointmentsBetween(from, until)
            if (NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
                val launchIntent = applicationContext.packageManager
                    .getLaunchIntentForPackage(applicationContext.packageName)
                    ?: Intent(applicationContext, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    applicationContext,
                    77_101,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.APPOINTMENTS)
                    .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                    .setContentTitle(applicationContext.getString(R.string.notification_daily_title))
                    .setContentText(applicationContext.getString(R.string.notification_daily_body, count))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()
                runCatching { NotificationManagerCompat.from(applicationContext).notify(77_101, notification) }
            }
            Result.success()
        }.getOrElse { Result.retry() }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "ayda_daily_summary"

        fun schedule(context: Context) {
            val zone = ZoneId.systemDefault()
            val now = java.time.ZonedDateTime.now(zone)
            var next = now.toLocalDate().atTime(8, 0).atZone(zone)
            if (!next.isAfter(now)) next = next.plusDays(1)
            val delay = Duration.between(now, next).toMillis().coerceAtLeast(0L)
            val request = PeriodicWorkRequestBuilder<DailySummaryWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                request,
            )
        }
    }
}

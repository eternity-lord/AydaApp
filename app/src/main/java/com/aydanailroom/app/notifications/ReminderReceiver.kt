package com.aydanailroom.app.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.aydanailroom.app.MainActivity
import com.aydanailroom.app.R
import com.aydanailroom.app.data.AppDatabase
import com.aydanailroom.app.util.formatClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationChannels.create(context)
        val appointmentId = intent.getLongExtra(EXTRA_APPOINTMENT_ID, 0L)
        val name = intent.getStringExtra(EXTRA_CLIENT_NAME).orEmpty()
        val phone = intent.getStringExtra(EXTRA_CLIENT_PHONE).orEmpty()
        val startAt = intent.getLongExtra(EXTRA_START_AT, System.currentTimeMillis())
        val amount = intent.getLongExtra(EXTRA_AMOUNT, 0L)
        val prefs = context.getSharedPreferences("ayda_settings", Context.MODE_PRIVATE)
        val latinDigits = prefs.getBoolean("latin_digits", false)
        val time = formatClock(startAt, latinDigits)
        val body = context.getString(R.string.notification_appointment_body, name, time)
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent(context, MainActivity::class.java)
        launchIntent.putExtra(MainActivity.EXTRA_APPOINTMENT_ID, appointmentId)
        val contentIntent = PendingIntent.getActivity(
            context,
            appointmentId.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val builder = NotificationCompat.Builder(context, NotificationChannels.APPOINTMENTS)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle(context.getString(R.string.notification_appointment_title))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)

        val doneIntent = Intent(context, AppointmentActionReceiver::class.java)
            .setAction(AppointmentActionReceiver.ACTION_DONE)
            .putExtra(AppointmentActionReceiver.EXTRA_APPOINTMENT_ID, appointmentId)
            .putExtra(AppointmentActionReceiver.EXTRA_AMOUNT, amount)
        val donePending = PendingIntent.getBroadcast(
            context,
            appointmentId.hashCode() + 10_000,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        builder.addAction(android.R.drawable.ic_menu_my_calendar, context.getString(R.string.notification_action_done), donePending)

        if (phone.isNotBlank()) {
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(phone)}"))
            val dialPending = PendingIntent.getActivity(
                context,
                appointmentId.hashCode() + 20_000,
                dialIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            builder.addAction(android.R.drawable.ic_menu_call, context.getString(R.string.notification_action_call), dialPending)
        }

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            runCatching { NotificationManagerCompat.from(context).notify(appointmentId.hashCode(), builder.build()) }
        }
    }

    companion object {
        const val EXTRA_APPOINTMENT_ID = "appointment_id"
        const val EXTRA_CLIENT_NAME = "client_name"
        const val EXTRA_CLIENT_PHONE = "client_phone"
        const val EXTRA_START_AT = "start_at"
        const val EXTRA_AMOUNT = "amount"
    }
}

class AppointmentActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DONE) return
        val pending = goAsync()
        val appointmentId = intent.getLongExtra(EXTRA_APPOINTMENT_ID, 0L)
        val amount = intent.getLongExtra(EXTRA_AMOUNT, 0L)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.get(context)
                val appointment = db.appDao().getAppointment(appointmentId)
                if (appointment != null) {
                    val repository = com.aydanailroom.app.data.AydaRepository(context, db)
                    repository.completeAppointment(appointmentId, amount)
                    ReminderScheduler.cancel(context, appointmentId)
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_DONE = "com.aydanailroom.app.action.APPOINTMENT_DONE"
        const val EXTRA_APPOINTMENT_ID = "appointment_id"
        const val EXTRA_AMOUNT = "amount"
    }
}

package com.aydanailroom.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.aydanailroom.app.R

object NotificationChannels {
    const val APPOINTMENTS = "appointment_reminders"
    const val BIRTHDAYS = "client_birthdays"
    const val BACKUPS = "backup_reminders"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channels = listOf(
            NotificationChannel(
                APPOINTMENTS,
                context.getString(R.string.notification_channel_appointments),
                NotificationManager.IMPORTANCE_HIGH,
            ),
            NotificationChannel(
                BIRTHDAYS,
                context.getString(R.string.notification_channel_birthdays),
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
            NotificationChannel(
                BACKUPS,
                context.getString(R.string.notification_channel_backup),
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
        manager.createNotificationChannels(channels)
    }
}

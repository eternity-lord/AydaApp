package com.aydanailroom.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.aydanailroom.app.data.AppointmentEntity

object ReminderScheduler {
    fun schedule(context: Context, appointment: AppointmentEntity, clientName: String, phone: String) {
        val reminderMinutes = appointment.reminderMinutesBefore
        if (appointment.status != "booked" || reminderMinutes == null) {
            cancel(context, appointment.id)
            return
        }
        val triggerAt = appointment.startAt - reminderMinutes * 60_000L
        if (triggerAt <= System.currentTimeMillis()) {
            cancel(context, appointment.id)
            return
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = reminderPendingIntent(context, appointment, clientName, phone)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancel(context: Context, appointmentId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            appointmentId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pending)
        pending.cancel()
    }

    private fun reminderPendingIntent(
        context: Context,
        appointment: AppointmentEntity,
        clientName: String,
        phone: String,
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(ReminderReceiver.EXTRA_APPOINTMENT_ID, appointment.id)
            .putExtra(ReminderReceiver.EXTRA_CLIENT_NAME, clientName)
            .putExtra(ReminderReceiver.EXTRA_CLIENT_PHONE, phone)
            .putExtra(ReminderReceiver.EXTRA_START_AT, appointment.startAt)
            .putExtra(ReminderReceiver.EXTRA_AMOUNT, (appointment.totalPrice - appointment.discount).coerceAtLeast(0L))
        return PendingIntent.getBroadcast(
            context,
            appointment.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}

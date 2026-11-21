package com.aydanailroom.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aydanailroom.app.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.get(context).appDao()
                val now = System.currentTimeMillis()
                dao.upcomingBookedAppointments(now).forEach { appointment ->
                    val client = dao.getClient(appointment.clientId) ?: return@forEach
                    ReminderScheduler.schedule(context, appointment, client.fullName, client.phone)
                }
                DailySummaryWorker.schedule(context)
            } finally {
                pending.finish()
            }
        }
    }
}

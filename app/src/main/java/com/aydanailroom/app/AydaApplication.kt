package com.aydanailroom.app

import android.app.Application
import com.aydanailroom.app.notifications.DailySummaryWorker
import com.aydanailroom.app.notifications.NotificationChannels

class AydaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.create(this)
        DailySummaryWorker.schedule(this)
    }
}

package com.runners.app

import android.app.Application
import android.content.Context
import com.google.android.gms.ads.MobileAds

class RunnersApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        MobileAds.initialize(this)
    }

    companion object {
        @Volatile
        lateinit var appContext: Context
            private set
    }
}

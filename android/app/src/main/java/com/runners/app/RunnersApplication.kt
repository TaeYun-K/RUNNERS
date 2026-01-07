package com.runners.app

import android.app.Application
import android.content.Context

class RunnersApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object {
        @Volatile
        lateinit var appContext: Context
            private set
    }
}


package de.wpaul.fritztempviewer

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        loggerClient = LoggerClient(this)
        instance = this
    }

    lateinit var loggerClient: LoggerClient
        private set

    companion object {
        lateinit var instance: App
    }
}
package com.example.clapp

import android.app.Application
import android.content.Context

class AppContextProvider : Application() {
    companion object {
        lateinit var context: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}

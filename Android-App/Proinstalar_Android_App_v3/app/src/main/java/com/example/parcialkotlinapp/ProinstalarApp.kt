package com.example.parcialkotlinapp

import android.app.Application

class ProinstalarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
    }
}

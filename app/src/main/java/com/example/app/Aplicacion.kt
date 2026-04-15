package com.example.app

import android.app.Application
import com.example.app.Auth.AuthManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Aplicacion : Application() {
    override fun onCreate() {
        super.onCreate()
        AuthManager.init(this)
    }
}
package com.example.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Aplicacion : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
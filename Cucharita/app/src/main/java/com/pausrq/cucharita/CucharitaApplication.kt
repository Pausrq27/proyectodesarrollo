package com.pausrq.cucharita

import android.app.Application
import com.pausrq.cucharita.api.ApiClient

class CucharitaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.initialize(this)
    }
}
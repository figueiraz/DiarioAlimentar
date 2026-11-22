package com.example.diarioalimentar

import android.app.Application
import com.example.diarioalimentar.data.local.AppDatabase

class DiarioAlimentarApp : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: DiarioAlimentarApp
            private set
    }
}

package com.example.graduatework.app

import android.app.Application
import com.example.graduatework.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class GraduateWorkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupTimber()
        startKoin {
            androidContext(this@GraduateWorkApplication)
            modules(appModule)
        }
    }

    private fun setupTimber() {
        Timber.plant(Timber.DebugTree())
    }
}
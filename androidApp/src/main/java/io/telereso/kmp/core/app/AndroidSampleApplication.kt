package io.telereso.kmp.core.app

import android.app.Application
import io.telereso.kmp.core.initLogger

class AndroidSampleApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        initLogger()
    }
}
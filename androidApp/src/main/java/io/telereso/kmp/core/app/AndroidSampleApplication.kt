package io.telereso.kmp.core.app

import android.app.Application
import io.telereso.kmp.core.CoreClient

class AndroidSampleApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        CoreClient.debugLogger()
    }
}
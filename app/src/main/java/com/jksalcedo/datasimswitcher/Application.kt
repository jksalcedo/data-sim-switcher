package com.jksalcedo.datasimswitcher

import android.app.Application
import com.topjohnwu.superuser.Shell

class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize libsu globally
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setTimeout(10)
        )
    }
}
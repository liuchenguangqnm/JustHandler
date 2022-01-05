package com.example.justhandler

import android.app.Application

/**
 * created by: Sunshine at 2021/11/22
 * desc:
 */
class MyApplication : Application() {
    companion object {
        var context: Application? = null
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }

    override fun onTerminate() {
        context = null
        super.onTerminate()
    }
}
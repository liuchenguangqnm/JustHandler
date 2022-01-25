package com.sunshine.justhandler.safe

import android.util.Log

/**
 * created by: Sunshine at 2021/11/25
 * desc:
 */
internal class SaveCheck {
    companion object {
        fun isSave() {
            val traceElement = Thread.currentThread().stackTrace
            for (element in traceElement) {
                Log.i("heihei", element.className)
            }
        }
    }
}
package com.sunshine.justhandler.excutor

import android.os.Handler
import android.os.Looper

/**
 * created by: Sunshine at 2021/11/23
 * desc: Ui 线程切换 Executor
 */
internal class UiExecutor {
    companion object {
        private val UI_HANDLER = Handler(Looper.getMainLooper())

        fun execute(runnable: Runnable) {
            UI_HANDLER.post(runnable)
        }

        fun getHandler(): Handler {
            return UI_HANDLER
        }
    }
}
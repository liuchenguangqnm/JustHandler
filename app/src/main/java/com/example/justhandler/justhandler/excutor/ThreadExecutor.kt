package com.example.justhandler.justhandler.excutor

import android.os.Handler
import android.os.Looper
import androidx.annotation.GuardedBy
import java.util.concurrent.Executors

/**
 * created by: Sunshine at 2021/11/23
 * desc: 子线程切换 Executor
 */
internal class ThreadExecutor {
    companion object {
        private val THREAD_POOL = Executors.newCachedThreadPool()

        @Volatile
        private var THREAD_HANDLER: Handler? = null

        fun execute(runnable: Runnable) {
            THREAD_POOL.execute(runnable)
        }

        @GuardedBy("ThreadExecutor.class")
        fun getHandler(): Handler {
            while (THREAD_HANDLER == null) {
                THREAD_POOL.execute {
                    if (Looper.myLooper() == null) Looper.prepare()
                    THREAD_HANDLER = Handler(Looper.myLooper()!!) {
                        if (it.callback != null) it.callback.run()
                        true
                    }
                    Looper.loop()
                }
            }
            return THREAD_HANDLER!!
        }
    }
}
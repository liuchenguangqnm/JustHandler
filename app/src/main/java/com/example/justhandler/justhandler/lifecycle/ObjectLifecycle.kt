package com.example.justhandler.justhandler.lifecycle

import android.util.Log
import com.example.justhandler.justhandler.JustHandler
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * created by: Sunshine at 2021/11/28
 * desc: 默认 Object 对象的生命周期监听，gc回收代表生命周期结束
 */
internal class ObjectLifecycle(private val target: Any, private val callback: Runnable) {
    companion object {
        private val referenceQueue = ReferenceQueue<Any>()
        private val lifecycleCatch = HashMap<Int, Lifecycle>()
        private val watcherCatch = HashMap<Int, WeakReference<Any>>()
        private val bridgeCatch = HashMap<Int, Int>()
        private var executor: ExecutorService? = null
    }

    init {
        val refKey = target.hashCode()
        synchronized(ObjectLifecycle::class.java) {
            if (!watcherCatch.containsKey(refKey)) {
                val lifecycleRef = WeakReference(target, referenceQueue)
                watcherCatch[refKey] = lifecycleRef
                val lifecycleKey = lifecycleRef.hashCode()
                lifecycleCatch[lifecycleKey] = JustHandler.getLifecycle(target)
                bridgeCatch[lifecycleKey] = refKey
            }
            startPoll()
            callback.run()
        }
    }

    private fun startPoll() {
        if (executor != null) return
        executor = Executors.newSingleThreadExecutor()
        executor!!.execute {
            while (true) {
                val reference = referenceQueue.remove()
                Log.i("ObjectLifecycle", "$reference")
                if (reference == null) continue
                val lifecycleKey = reference.hashCode()
                synchronized(ObjectLifecycle::class.java) {
                    lifecycleCatch.remove(lifecycleKey)?.onDestroy()
                    watcherCatch.remove(bridgeCatch[lifecycleKey])
                    bridgeCatch.remove(lifecycleKey)
                }
            }
        }
    }
}
package com.example.justhandler

import android.util.Log
import com.example.justhandler.justhandler.JustHandler
import com.example.justhandler.justhandler.invoke.InvokeFun

/**
 * created by: Sunshine at 2021/11/28
 * desc:
 */
class People {
    private val lifecycle = JustHandler.getLifecycle(People::class.java)

    fun register() {
        JustHandler.getEventInMain(this, object : InvokeFun("100") {
            override fun invoke(obj: Any?) {
                Log.i("haha1", "$obj=======${Thread.currentThread().name}")
            }
        })

        JustHandler.getEventInMain(this, object : InvokeFun("200") {
            override fun invoke(obj: Any?) {
                Log.i("haha1", "$obj=======${Thread.currentThread().name}")
            }
        })

        JustHandler.getEventInThread(this, object : InvokeFun("100") {
            override fun invoke(obj: Any?) {
                Log.i("haha2", "$obj=======${Thread.currentThread().name}")
            }
        })

        JustHandler.getEventInThread(this, object : InvokeFun("200") {
            override fun invoke(obj: Any?) {
                Log.i("haha2", "$obj=======${Thread.currentThread().name}")
            }
        })
    }

    fun onDestroyToMsgTag(vararg msgTags: String) {
        lifecycle.onDestroyToMsgTag(*msgTags)
    }
}
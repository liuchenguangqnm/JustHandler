package com.example.justhandler

import android.util.Log
import com.example.justhandler.justhandler.JustHandler
import com.example.justhandler.justhandler.invoke.InvokeFun
import com.example.justhandler.justhandler.invoke.InvokeThreadType

/**
 * created by: Sunshine at 2021/11/28
 * desc:
 */
class People {
    private val lifecycle = JustHandler.getLifecycle(this)

    fun register() {
        JustHandler.getEvent(
            this, object : InvokeFun("100", InvokeThreadType.MAIN_THREAD) {
                override fun invoke(obj: Any?) {
                    Log.i("People", "$obj=======${Thread.currentThread().name}")
                }
            })

        JustHandler.getEvent(
            this, object : InvokeFun("200", InvokeThreadType.RANDOM_THREAD) {
                override fun invoke(obj: Any?) {
                    Log.i("People", "$obj=======${Thread.currentThread().name}")
                }
            })
    }

    fun onDestroyToMsgTag(vararg msgTags: String) {
        lifecycle.onDestroyToMsgTag(*msgTags)
    }
}
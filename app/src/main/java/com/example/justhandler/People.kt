package com.example.justhandler

import android.util.Log
import com.sunshine.justhandler.JustHandler
import com.sunshine.justhandler.invoke.InvokeFun
import com.sunshine.justhandler.invoke.InvokeThreadType

/**
 * created by: Sunshine at 2021/11/28
 * desc:
 */
class People {
    private val lifecycle = JustHandler.getLifecycle(this)

    fun register() {
        JustHandler.getMsg(
            this, object : InvokeFun("100", InvokeThreadType.MAIN_THREAD) {
                override fun invoke(obj: Any?) {
                    Log.i("People", "$obj=======${Thread.currentThread().name}")
                }
            })

        JustHandler.getMsg(
            this, object : InvokeFun("200", InvokeThreadType.SEND_THREAD) {
                override fun invoke(obj: Any?) {
                    Log.i("People", "$obj=======${Thread.currentThread().name}")
                }
            })
    }

    fun onDestroyToMsgTag(vararg msgTags: String) {
        lifecycle.onDestroyToMsgTag(*msgTags)
    }
}
package com.example.justhandler.justhandler.invoke

import androidx.annotation.GuardedBy

/**
 * created by: Sunshine at 2021/11/23
 * desc: 消息回调包装者
 */
internal class InvokeWrapper {
    @Volatile
    private var invokes = arrayListOf<InvokeFun>()

    @Volatile
    var isActive = true

    private fun checkIsActive(): Boolean {
        if (!isActive) clearInvoke()
        return isActive
    }

    @GuardedBy("this")
    fun addInvoke(invokeFun: InvokeFun) {
        if (!checkIsActive() || invokes.contains(invokeFun)) return
        val copyInvokes = ArrayList(invokes)
        copyInvokes.add(invokeFun)
        invokes = copyInvokes
    }

    @GuardedBy("this")
    fun removeInvoke(msgTag: String) {
        if (!checkIsActive() || invokes.isEmpty()) return
        val copyInvokes = ArrayList(invokes)
        for (index in copyInvokes.lastIndex downTo 0) {
            if (copyInvokes[index].msgTag == msgTag) copyInvokes.removeAt(index)
        }
        invokes = copyInvokes
    }

    @GuardedBy("this")
    fun getInvoke(msgTag: String): InvokeFun? {
        var reslut: InvokeFun? = null
        if (!checkIsActive() || invokes.isEmpty()) return reslut
        invokes.map {
            if (it.msgTag == msgTag) reslut = it
        }
        // TODO 为什么在密集发送消息的时候，result偶尔会取值为null
        return reslut
    }

    @GuardedBy("this")
    fun clearInvoke() {
        invokes = arrayListOf()
    }
}
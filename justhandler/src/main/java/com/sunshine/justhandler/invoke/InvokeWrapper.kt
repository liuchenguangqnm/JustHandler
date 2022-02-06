package com.sunshine.justhandler.invoke

import androidx.annotation.GuardedBy
import java.util.*
import kotlin.collections.ArrayList

/**
 * created by: Sunshine at 2021/11/23
 * desc: 消息回调包装者
 */
internal class InvokeWrapper {
    @Volatile
    private var invokes = arrayListOf<InvokeFun>()

    @Volatile
    var isActive = true

    @GuardedBy("this")
    fun addInvoke(invokeFun: InvokeFun): Int {
        if (invokes.contains(invokeFun)) return invokes.size
        val copyInvokes = ArrayList(invokes)
        copyInvokes.add(invokeFun)
        invokes = copyInvokes
        return invokes.size
    }

    @GuardedBy("this")
    fun removeInvoke(msgTag: String): Int {
        if (invokes.isEmpty()) return invokes.size
        val copyInvokes = ArrayList(invokes)
        for (index in copyInvokes.lastIndex downTo 0) {
            if (copyInvokes[index].msgTag == msgTag) copyInvokes.removeAt(index)
        }
        invokes = copyInvokes
        return invokes.size
    }

    @GuardedBy("this")
    fun getInvokes(): List<InvokeFun> {
        return LinkedList(invokes)
    }
}
package com.example.justhandler.justhandler.invoke

/**
 * created by: Sunshine at 2021/11/23
 * desc: 消息回调包装者
 */
internal class InvokeWrapper {
    @Volatile
    var invokes = arrayListOf<InvokeFun>()

    @Volatile
    var isActive = true
}
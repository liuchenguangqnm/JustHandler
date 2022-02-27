package com.example.justhandler.testBean

import android.os.Handler
import android.os.Looper
import java.lang.Exception

/**
 * created by: Sunshine at 2022/2/27
 * desc:
 */
data class MsgBean(
    val tag: String,
    val content: String,
    val list: List<*> = mutableListOf<Any?>(
        0, "asdf", Handler(Looper.getMainLooper()), Exception()
    ),
    val map: Map<*, *> = mutableMapOf<Any?, Any?>(
        Pair(10, 0), Pair("ksksk", "asdf"),
        Pair(100L, Handler(Looper.getMainLooper())), Pair(10.1, Exception())
    )
)
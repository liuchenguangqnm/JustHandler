package com.example.justhandler.testBean

import java.lang.Exception

/**
 * created by: Sunshine at 2022/2/27
 * desc:
 */
data class MsgBean(
    val tag: String,
    val content: String,
    val list: List<String> = mutableListOf("asdf", "asdf1"),
    val map: Map<*, *> = mutableMapOf<Any?, Any?>(
        Pair(10, 0), Pair("ksksk", "asdf"),
        Pair(Test(), "asdf"), Pair(100L, "asdf"), Pair(10f, Exception("我尼玛这是个异常"))
    )
)
package com.example.justhandler.justhandler.lifecycle

/**
 * created by: Sunshine at 2021/11/23
 * desc: LifeCycle 接口
 */
interface Lifecycle {
    fun onDestroy()

    fun onDestroyToMsgTag(vararg msgTags: String)
}
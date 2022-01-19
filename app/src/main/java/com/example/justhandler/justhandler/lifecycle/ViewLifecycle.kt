package com.example.justhandler.justhandler.lifecycle

import android.view.View

/**
 * created by: Sunshine at 2021/11/29
 * desc: View 的附着状态监听
 */
class ViewLifecycle(private val lifecycle: Lifecycle) : View.OnAttachStateChangeListener {
    override fun onViewAttachedToWindow(v: View?) {
    }

    override fun onViewDetachedFromWindow(v: View?) {
        lifecycle.onDestroy()
    }
}
package com.example.justhandler.justhandler.lifecycle

import android.app.Fragment

/**
 * created by: Sunshine at 2021/11/23
 * desc: 旧api的 LifecycleFragment
 */
internal class LifecycleFr : Fragment() {
    private var lifecycle: Lifecycle? = null

    fun setLifecycle(lifecycle: Lifecycle) {
        this.lifecycle = lifecycle
    }

    override fun onDestroy() {
        lifecycle?.onDestroy()
        super.onDestroy()
    }
}
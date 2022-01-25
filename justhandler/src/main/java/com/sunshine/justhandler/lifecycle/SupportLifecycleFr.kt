package com.sunshine.justhandler.lifecycle

import androidx.fragment.app.Fragment

/**
 * created by: Sunshine at 2021/11/23
 * desc: 生命周期监听 LifecycleFragment
 */
internal class SupportLifecycleFr : Fragment() {
    private var lifecycle: Lifecycle? = null

    fun setLifecycle(lifecycle: Lifecycle) {
        this.lifecycle = lifecycle
    }

    override fun onDestroy() {
        lifecycle?.onDestroy()
        super.onDestroy()
    }
}
package com.example.justhandler.justhandler.lifecycle

import android.app.Activity
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.justhandler.justhandler.JustHandler
import com.example.justhandler.justhandler.excutor.ThreadExecutor
import com.example.justhandler.justhandler.excutor.UiExecutor

/**
 * created by: Sunshine at 2021/11/23
 * desc: 给目标页面附着生命监听代理Fragment
 */
internal class AttachLifecycle {
    companion object {
        private const val FRAGMENT_TAG = "justhandler.lifecycle"

        fun attachLifecycle(target: Any, callback: Runnable) {
            when (target) {
                is FragmentActivity -> {
                    attachLifecycle(target, callback)
                }
                is Fragment -> {
                    attachLifecycle(target, callback)
                }
                is Activity -> {
                    attachLifecycle(target, callback)
                }
                is android.app.Fragment -> {
                    attachLifecycle(target, callback)
                }
                is View -> {
                    attachLifecycle(target, callback)
                }
                else -> {
                    ThreadExecutor.execute {
                        ObjectLifecycle(target, callback)
                    }
                }
            }
        }

        private fun attachLifecycle(activity: FragmentActivity, callback: Runnable) {
            UiExecutor.execute {
                val fm = activity.supportFragmentManager
                val frTag = makeFragmentTag(activity)
                val findFr = fm.findFragmentByTag(frTag)
                if (findFr == null) {
                    val lifecycle = JustHandler.getLifecycle(activity)
                    val lifecycleFr = SupportLifecycleFr()
                    lifecycleFr.setLifecycle(lifecycle)
                    fm.beginTransaction().add(lifecycleFr, frTag).commitAllowingStateLoss()
                }
                callback.run()
            }
        }

        private fun attachLifecycle(fragment: Fragment, callback: Runnable) {
            UiExecutor.execute {
                val fm = fragment.childFragmentManager
                val frTag = makeFragmentTag(fragment)
                val findFr = fm.findFragmentByTag(frTag)
                if (findFr == null) {
                    val lifecycle = JustHandler.getLifecycle(fragment)
                    val lifecycleFr = SupportLifecycleFr()
                    lifecycleFr.setLifecycle(lifecycle)
                    fm.beginTransaction().add(lifecycleFr, frTag).commitAllowingStateLoss()
                }
                callback.run()
            }
        }

        private fun attachLifecycle(activity: Activity, callback: Runnable) {
            UiExecutor.execute {
                val fm = activity.fragmentManager
                val frTag = makeFragmentTag(activity)
                val findFr = fm.findFragmentByTag(frTag)
                if (findFr == null) {
                    val lifecycle = JustHandler.getLifecycle(activity)
                    val lifecycleFr = LifecycleFr()
                    lifecycleFr.setLifecycle(lifecycle)
                    fm.beginTransaction().add(lifecycleFr, frTag).commitAllowingStateLoss()
                }
                callback.run()
            }
        }

        private fun attachLifecycle(fragment: android.app.Fragment, callback: Runnable) {
            UiExecutor.execute {
                val fm = fragment.childFragmentManager
                val frTag = makeFragmentTag(fragment)
                val findFr = fm.findFragmentByTag(frTag)
                if (findFr == null) {
                    val lifecycle = JustHandler.getLifecycle(fragment)
                    val lifecycleFr = LifecycleFr()
                    lifecycleFr.setLifecycle(lifecycle)
                    fm.beginTransaction().add(lifecycleFr, frTag).commitAllowingStateLoss()
                }
                callback.run()
            }
        }

        private fun attachLifecycle(view: View, callback: Runnable) {
            UiExecutor.execute {
                val listener = ViewLifecycle(JustHandler.getLifecycle(view))
                view.addOnAttachStateChangeListener(listener)
                callback.run()
            }
        }

        private fun makeFragmentTag(target: Any): String {
            return "$FRAGMENT_TAG${target.javaClass.canonicalName}"
        }
    }
}
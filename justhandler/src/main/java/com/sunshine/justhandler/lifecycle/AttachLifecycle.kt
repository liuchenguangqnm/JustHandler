package com.sunshine.justhandler.lifecycle

import android.app.Activity
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.sunshine.justhandler.JustHandler
import com.sunshine.justhandler.excutor.ThreadExecutor
import com.sunshine.justhandler.excutor.UiExecutor
import java.lang.ref.WeakReference

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
            val weakTarget = WeakReference(activity)
            UiExecutor.execute {
                val target = weakTarget.get() ?: return@execute
                if (target.isDestroyed) return@execute
                val fm = target.supportFragmentManager
                val frTag = makeFragmentTag(target)
                val findFr = fm.findFragmentByTag(frTag)
                if (findFr == null) {
                    val lifecycle = JustHandler.getLifecycle(target)
                    val lifecycleFr = SupportLifecycleFr()
                    lifecycleFr.setLifecycle(lifecycle)
                    fm.beginTransaction().add(lifecycleFr, frTag).commitAllowingStateLoss()
                }
                callback.run()
            }
        }

        private fun attachLifecycle(fragment: Fragment, callback: Runnable) {
            val weakTarget = WeakReference(fragment)
            UiExecutor.execute {
                val target = weakTarget.get() ?: return@execute
                if (target.isAdded) {
                    val fm = target.childFragmentManager
                    val frTag = makeFragmentTag(target)
                    val findFr = fm.findFragmentByTag(frTag)
                    if (findFr == null) {
                        val lifecycle = JustHandler.getLifecycle(target)
                        val lifecycleFr = SupportLifecycleFr()
                        lifecycleFr.setLifecycle(lifecycle)
                        fm.beginTransaction().add(lifecycleFr, frTag).commitAllowingStateLoss()
                    }
                    callback.run()
                } else {
                    Looper.myQueue().addIdleHandler {
                        attachLifecycle(target, callback)
                        false
                    }
                }
            }
        }

        private fun attachLifecycle(activity: Activity, callback: Runnable) {
            val weakTarget = WeakReference(activity)
            UiExecutor.execute {
                val target = weakTarget.get() ?: return@execute
                if (target.isDestroyed) return@execute
                val fm = target.fragmentManager
                val frTag = makeFragmentTag(target)
                val findFr = fm.findFragmentByTag(frTag)
                if (findFr == null) {
                    val lifecycle = JustHandler.getLifecycle(target)
                    val lifecycleFr = LifecycleFr()
                    lifecycleFr.setLifecycle(lifecycle)
                    fm.beginTransaction().add(lifecycleFr, frTag).commitAllowingStateLoss()
                }
                callback.run()
            }
        }

        private fun attachLifecycle(fragment: android.app.Fragment, callback: Runnable) {
            val weakTarget = WeakReference(fragment)
            UiExecutor.execute {
                val target = weakTarget.get() ?: return@execute
                if (target.isAdded) {
                    val fm = target.childFragmentManager
                    val frTag = makeFragmentTag(target)
                    val findFr = fm.findFragmentByTag(frTag)
                    if (findFr == null) {
                        val lifecycle = JustHandler.getLifecycle(target)
                        val lifecycleFr = LifecycleFr()
                        lifecycleFr.setLifecycle(lifecycle)
                        fm.beginTransaction().add(lifecycleFr, frTag).commitAllowingStateLoss()
                    }
                    callback.run()
                } else {
                    if (target.childFragmentManager?.isDestroyed != false) return@execute
                    Looper.myQueue().addIdleHandler {
                        attachLifecycle(target, callback)
                        false
                    }
                }
            }
        }

        private fun attachLifecycle(view: View, callback: Runnable) {
            val weakTarget = WeakReference(view)
            UiExecutor.execute {
                val target = weakTarget.get() ?: return@execute
                val listener = ViewLifecycle(JustHandler.getLifecycle(target))
                target.addOnAttachStateChangeListener(listener)
                callback.run()
            }
        }

        private fun makeFragmentTag(target: Any): String {
            return "$FRAGMENT_TAG${target.javaClass.canonicalName}"
        }
    }
}
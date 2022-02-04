package com.sunshine.justhandler.message

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.sunshine.justhandler.Register
import com.sunshine.justhandler.excutor.ThreadExecutor
import com.sunshine.justhandler.excutor.UiExecutor
import com.sunshine.justhandler.invoke.InvokeFun
import com.sunshine.justhandler.invoke.InvokeThreadType
import java.lang.ref.WeakReference

/**
 * created by: Sunshine at 2021/11/24
 * desc: JustHandler 消息工厂
 */
internal class MessageFactory {
    companion object {
        /**
         * Message 构造方法
         * @param msgTag 消息甄别 messageTag
         * @param data    消息携带数据
         * @param handler 接收此 Message 的 Handler
         */
        fun buildMessage(msgTag: String, data: Any?, handler: Handler): Message {
            // 获取发送方线程实体（这里用弱引用防止callback直接持有Thread引发内存泄漏）
            val sendThreadWeak = WeakReference(Thread.currentThread())
            val isSendInMain = Looper.myLooper() == Looper.getMainLooper()
            // callback构建
            val callback = Runnable {
                ThreadExecutor.execute {
                    val invokeMapLifeCycle = Register.InvokeWrappers
                    val lifeCycleKeys = HashSet(invokeMapLifeCycle.keys)
                    for (key in lifeCycleKeys) {
                        val invokeWrapper = invokeMapLifeCycle[key] ?: return@execute
                        val invokeFunList = invokeWrapper.getInvokes(msgTag)
                        for (invokeFun in invokeFunList) {
                            // 如果外层包裹invoke不再活跃，则不能继续发送回调
                            if (!invokeWrapper.isActive) break
                            // 在不同的线程发送回调
                            when (invokeFun.invokeThread) {
                                InvokeThreadType.MAIN_THREAD -> {
                                    UiExecutor.execute {
                                        invokeFun.invoke(data)
                                    }
                                }
                                InvokeThreadType.SEND_THREAD -> {
                                    val sendThread = sendThreadWeak.get() ?: return@execute
                                    if (isSendInMain) UiExecutor.execute {
                                        invokeFun.invoke(data)
                                    } else invokeInThread(sendThread, invokeFun, data)
                                }
                                else -> {
                                    invokeFun.invoke(data)
                                }
                            }
                        }
                    }
                }
            }
            return Message.obtain(handler, callback)
        }

        // 在指定线程中执行回调方法
        @SuppressLint("DiscouragedPrivateApi")
        private fun invokeInThread(thread: Thread, getInvoke: InvokeFun?, data: Any?) {
            try {
                thread.join()
            } catch (e: InterruptedException) {
                // ignore
            }

            val target = try {
                thread.javaClass.getDeclaredField("target")
            } catch (e: NoSuchFieldException) {
                null
            } ?: return
            target.isAccessible = true
            val oldTarget = target.get(thread)
            if (thread.state == Thread.State.TERMINATED) target.set(thread, object : Runnable {
                override fun run() {
                    getInvoke?.invoke(data)
                    target.set(thread, oldTarget)
                    target.isAccessible = false
                }
            })
            if (thread.state == Thread.State.TERMINATED) thread.start()
        }
    }
}
package com.sunshine.justhandler.sender

import android.os.Looper
import android.os.Message
import com.sunshine.justhandler.register.Register
import com.sunshine.justhandler.excutor.ThreadExecutor
import com.sunshine.justhandler.excutor.UiExecutor
import com.sunshine.justhandler.invoke.InvokeThreadType

/**
 * created by: Sunshine at 2021/11/24
 * desc: JustHandler 事件发送者
 */
internal class MessageSender {
    companion object {
        /**
         * Message 事件发送入口方法
         * @param msgTag 消息甄别 messageTag
         * @param data    消息携带数据
         * @param post   消息延迟响应毫秒数
         */
        fun sendMessage(msgTag: String, data: Any?, post: Long) {
            asyncDispatch(msgTag, data, post)
            dispatch(msgTag, data, post)
        }

        // 异步分发事件
        private fun asyncDispatch(msgTag: String, data: Any?, post: Long) {
            val handler = ThreadExecutor.getHandler()
            val callback = Runnable {
                ThreadExecutor.execute {
                    // 常用参数获取
                    val lifecycles = Register.invokeLifecycles[msgTag] ?: return@execute
                    val invokeKeys = HashSet(lifecycles)
                    // 将事件分发至UI线程或任意子线程
                    for (key in invokeKeys) {
                        val invokeWrapper = Register.invokeWrappers[key] ?: return@execute
                        for (invokeFun in invokeWrapper.getInvokes()) {
                            // 如果外层包裹invoke不再活跃，则不能继续分发事件
                            if (!invokeWrapper.isActive) break
                            // 不属于相关Tag，不予分发
                            if (msgTag != invokeFun.msgTag) continue
                            // 在不同的线程发送回调
                            when (invokeFun.invokeThread) {
                                InvokeThreadType.MAIN_THREAD -> {
                                    // 在主线程回调
                                    UiExecutor.execute {
                                        invokeFun.invoke(data)
                                    }
                                }
                                else -> {
                                    // 在任意线程回调
                                    ThreadExecutor.execute {
                                        invokeFun.invoke(data)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            val message = Message.obtain(handler, callback)
            handler.sendMessageDelayed(message, post)
        }

        // 同步分发事件
        private fun dispatch(msgTag: String, data: Any?, post: Long) {
            // 如果事件发送方为UI线程，则仍使用异步分发逻辑
            if (Looper.myLooper() == Looper.getMainLooper()) {
                val handler = ThreadExecutor.getHandler()
                val callback = Runnable {
                    ThreadExecutor.execute {
                        // 常用参数获取
                        val lifecycles = Register.threadInvokeLifecycles[msgTag] ?: return@execute
                        val threadInvokeKeys = HashSet(lifecycles)
                        // 将事件分发至UI线程
                        for (key in threadInvokeKeys) {
                            val invokeWrapper = Register.threadInvokeWrappers[key] ?: return@execute
                            for (invokeFun in invokeWrapper.getInvokes()) {
                                // 如果外层包裹invoke不再活跃，则不能继续分发事件
                                if (!invokeWrapper.isActive) break
                                // 不属于相关Tag，不予分发
                                if (msgTag != invokeFun.msgTag) continue
                                // 在发送方线程回调（异步回调仅支持UI线程）
                                if (Looper.myLooper() == Looper.getMainLooper()) UiExecutor.execute {
                                    invokeFun.invoke(data)
                                }
                            }
                        }
                    }
                }
                val message = Message.obtain(handler, callback)
                handler.sendMessageDelayed(message, post)
            } else {
                // 常用参数获取
                val lifecycles = Register.threadInvokeLifecycles[msgTag] ?: return
                val threadInvokeKeys = HashSet(lifecycles)
                // 将事件同步分发至消息发送方的线程
                for (key in threadInvokeKeys) {
                    val invokeWrapper = Register.threadInvokeWrappers[key] ?: continue
                    for (invokeFun in invokeWrapper.getInvokes()) {
                        // 如果外层包裹invoke不再活跃，则不能继续分发事件
                        if (!invokeWrapper.isActive) break
                        // 不属于相关Tag，不予分发
                        if (msgTag != invokeFun.msgTag) continue
                        // 事件发送方是子线程，直接进行分发
                        if (post > 0L) Thread.sleep(post)
                        invokeFun.invoke(data)
                    }
                }
            }
        }
    }
}
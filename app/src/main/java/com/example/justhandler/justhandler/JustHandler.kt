package com.example.justhandler.justhandler

import android.annotation.SuppressLint
import android.app.Application
import android.app.Service
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.annotation.GuardedBy
import com.example.justhandler.justhandler.excutor.ThreadExecutor
import com.example.justhandler.justhandler.excutor.UiExecutor
import com.example.justhandler.justhandler.invoke.InvokeFun
import com.example.justhandler.justhandler.invoke.InvokeThreadType
import com.example.justhandler.justhandler.invoke.InvokeWrapper
import com.example.justhandler.justhandler.lifecycle.AttachLifecycle
import com.example.justhandler.justhandler.lifecycle.Lifecycle
import java.lang.IllegalArgumentException
import java.lang.ref.WeakReference
import java.lang.reflect.Proxy

/**
 * created by: Sunshine at 2021/11/23
 * desc: 纯粹的 Handler 主要优势：
 * 1、支持全局消息延迟发送
 * 2、支持线程间通信，子线程发送的消息可在主线程接收，反之亦然
 * 3、在 Activity、Fragment、Entity 和 自定义View 中使用时无需关注内存泄漏，可随时随处发送、接收消息
 */
class JustHandler {
    companion object {
        /**
         * 发送消息
         * @param msgTag 消息甄别 messageTag
         */
        @JvmStatic
        fun sendMsg(msgTag: String): Companion {
            sendMsg(msgTag, null, 0)
            return Companion
        }

        /**
         * 发送消息
         * @param msgTag 消息甄别 messageTag
         * @param data   消息携带数据
         */
        @JvmStatic
        fun sendMsg(msgTag: String, data: Any? = null): Companion {
            sendMsg(msgTag, data, 0)
            return Companion
        }

        /**
         * 发送消息
         * @param msgTag 消息甄别 messageTag
         * @param data   消息携带数据
         * @param post   消息延迟响应毫秒数
         */
        @JvmStatic
        fun sendMsg(msgTag: String, data: Any? = null, post: Long = 0): Companion {
            val handler = ThreadExecutor.getHandler()
            val message = MessageFactory.buildMessage(msgTag, data, handler)
            handler.sendMessageDelayed(message, post)
            return Companion
        }

        /**
         * 响应消息
         * @param lifecycleTarget 非 Application/Service 对象
         * @param invoke          信息回调 Function
         */
        @JvmStatic
        fun getEvent(lifecycleTarget: Any, invoke: InvokeFun) {
            Register.eventRegister(lifecycleTarget, invoke)
        }

        /**
         * 获取 lifecycle 代理对象（开发者可用它来精确设定某个对象何时不再响应Message）
         * @param lifecycleTarget 非 Application/Service 对象
         *
         * 使用方法：
         * 1、val lifecycle = JustHandler.getLifecycle(lifecycleTarget = 希望精确设定停止响应Message时机的对象)
         * 2、lifecycle.onDestroy() // 此方法一旦被调用，lifecycle 对应的 lifecycleTarget 将不再响应任何 Message
         */
        @JvmStatic
        fun <T : Any> getLifecycle(lifecycleTarget: T): Lifecycle {
            return Register.getLifecycle(lifecycleTarget)
        }
    }
}

/**
 * created by: Sunshine at 2021/11/24
 * desc: JustHandler 消息工厂
 */
private class MessageFactory {
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
            // callback构建
            val callback = Runnable {
                ThreadExecutor.execute {
                    val invokeMapLifeCycle = Register.InvokeWrappers
                    val lifeCycleKeys = ArrayList(invokeMapLifeCycle.keys)
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
                                    invokeInThread(sendThread, invokeFun, data)
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
            if (!thread.isAlive) return
            val target = try {
                thread.javaClass.getDeclaredField("target")
            } catch (e: NoSuchFieldException) {
                null
            }
            kotlin.concurrent.thread {
                Log.i("JustHandler1", "$data=======${thread.name}")
                Log.i("JustHandler2", "$data=======${Thread.currentThread().name}")
            }
        }
    }
}

/**
 * created by: Sunshine at 2021/11/23
 * desc: 信息回调注册器
 */
private class Register {
    companion object {
        val InvokeWrappers = HashMap<Int, InvokeWrapper>()

        /**
         * 事件注册
         */
        fun <T : Any> eventRegister(target: T, invoke: InvokeFun) {
            // target不是Application/Activity/Fragment就不支持
            checkSupport(target.javaClass)
            // 附着 Lifecycle
            AttachLifecycle.attachLifecycle(target) {
                // Lifecycle 附着完成，在子线程修改缓存字典
                ThreadExecutor.execute {
                    insertInvoke(target.hashCode(), invoke)
                }
            }
        }

        // 获取LifeCycle
        fun getLifecycle(target: Any): Lifecycle {
            // target 不支持 Application/Service
            checkSupport(target.javaClass)
            // 获取字典key
            val key = target.hashCode()
            // 借助动态代理实现HashMap的维护
            return Proxy.newProxyInstance(
                Lifecycle::class.java.classLoader, arrayOf(Lifecycle::class.java)
            ) { _, method, args ->
                val methodName = method?.name ?: ""
                if (methodName == "onDestroy") {
                    // 不等线程阻塞修改invoke缓存
                    InvokeWrappers[key]?.isActive = false
                    InvokeWrappers[key]?.clearInvoke()
                    // 子线程修改缓存字典
                    ThreadExecutor.execute { removeInvoke(key) }
                } else if (methodName == "onDestroyToMsgTag") {
                    args.map { arg ->
                        if (arg is Array<*>) {
                            arg.map { argItem ->
                                if (argItem is String) removeInvoke(key, argItem)
                            }
                        }
                    }
                }
                methodName
            } as Lifecycle
        }

        @GuardedBy("Register.class")
        private fun insertInvoke(key: Int, invoke: InvokeFun) {
            // 尝试注册invoke回调
            if (InvokeWrappers[key] == null) {
                InvokeWrappers[key] = InvokeWrapper()
                InvokeWrappers[key]!!.addInvoke(invoke)
            } else if (InvokeWrappers[key]!!.isActive) {
                InvokeWrappers[key]!!.addInvoke(invoke)
            }
        }

        @GuardedBy("Register.class")
        private fun removeInvoke(key: Int, msgTag: String) {
            InvokeWrappers[key]?.removeInvoke(msgTag)
        }

        @GuardedBy("Register.class")
        private fun removeInvoke(key: Int) {
            InvokeWrappers.remove(key)
        }

        // 是否是支持的参数类型
        fun checkSupport(targetClass: Class<Any>) {
            val unSupport = Application::class.java.isAssignableFrom(targetClass) ||
                    Service::class.java.isAssignableFrom(targetClass)
            if (unSupport) {
                throw IllegalArgumentException("target 不支持 Application/Service")
            }
        }
    }
}
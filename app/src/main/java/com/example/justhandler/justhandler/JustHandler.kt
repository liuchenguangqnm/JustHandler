package com.example.justhandler.justhandler

import android.app.Application
import android.app.Service
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.annotation.GuardedBy
import com.example.justhandler.justhandler.excutor.ThreadExecutor
import com.example.justhandler.justhandler.excutor.UiExecutor
import com.example.justhandler.justhandler.invoke.InvokeFun
import com.example.justhandler.justhandler.invoke.InvokeWrapper
import com.example.justhandler.justhandler.lifecycle.AttachLifecycle
import com.example.justhandler.justhandler.lifecycle.Lifecycle
import java.lang.IllegalArgumentException
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
            sendMainMsg(msgTag, null, 0)
            sendThreadMsg(msgTag, null, 0)
            return Companion
        }

        /**
         * 发送消息
         * @param msgTag 消息甄别 messageTag
         * @param data    消息携带数据
         */
        @JvmStatic
        fun sendMsg(msgTag: String, data: Any? = null): Companion {
            sendMainMsg(msgTag, data, 0)
            sendThreadMsg(msgTag, data, 0)
            return Companion
        }

        /**
         * 发送消息
         * @param msgTag 消息甄别 messageTag
         * @param data    消息携带数据
         * @param post    消息延迟响应毫秒数
         */
        @JvmStatic
        fun sendMsg(msgTag: String, data: Any? = null, post: Long = 0): Companion {
            sendMainMsg(msgTag, data, post)
            sendThreadMsg(msgTag, data, post)
            return Companion
        }

        /**
         * 发送在主线程响应的消息
         * @param msgTag 消息甄别 messageTag
         * @param data    消息携带数据
         * @param post    消息延迟响应毫秒数
         */
        private fun sendMainMsg(msgTag: String, data: Any? = null, post: Long = 0): Companion {
            val handler = UiExecutor.getHandler()
            val message = MessageFactory.buildMessage(msgTag, data, handler)
            handler.sendMessageDelayed(message, post)
            return Companion
        }

        /**
         * 发送在子线程响应的消息
         * @param msgTag 消息甄别 messageTag
         * @param data    消息携带数据
         * @param post    消息延迟响应毫秒数
         */
        private fun sendThreadMsg(msgTag: String, data: Any? = null, post: Long = 0): Companion {
            val handler = ThreadExecutor.getHandler()
            val message = MessageFactory.buildMessage(msgTag, data, handler)
            handler.sendMessageDelayed(message, post)
            return Companion
        }

        /**
         * 获取主线程的消息
         * @param lifecycleTarget 非 Application/Service 对象
         * @param invoke          信息回调 Function
         */
        @JvmStatic
        fun getEventInMain(lifecycleTarget: Any, invoke: InvokeFun) {
            Register.eventRegister(lifecycleTarget, false, invoke)
        }

        /**
         * 获取子线程的消息
         * @param lifecycleTarget 非 Application/Service 对象
         * @param invoke          信息回调 Function
         */
        @JvmStatic
        fun getEventInThread(lifecycleTarget: Any, invoke: InvokeFun) {
            Register.eventRegister(lifecycleTarget, true, invoke)
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
            val callback = Runnable {
                ThreadExecutor.execute {
                    val isMainThread = handler.looper == Looper.getMainLooper()
                    // 正式逻辑
                    val invokeMapLifeCycle = if (isMainThread) Register.invokes
                    else Register.bgInvokes
                    val lifeCycleKeys = ArrayList(invokeMapLifeCycle.keys)
                    for (key in lifeCycleKeys) {
                        val invokeWrapper = invokeMapLifeCycle[key] ?: return@execute
                        if (!invokeWrapper.isActive) return@execute
                        for (invoke in invokeWrapper.invokes) {
                            if (!invoke.msgTags.contains(msgTag)) continue
                            if (!isMainThread) invoke.invoke(data)
                            else UiExecutor.execute { invoke.invoke(data) }
                        }
                    }
                }
            }
            return Message.obtain(handler, callback)
        }
    }
}

/**
 * created by: Sunshine at 2021/11/23
 * desc: 信息回调注册器
 */
private class Register {
    companion object {
        val invokes = HashMap<Int, InvokeWrapper>()
        val bgInvokes = HashMap<Int, InvokeWrapper>()

        // 事件注册
        fun <T : Any> eventRegister(
            target: T, isInBg: Boolean, invoke: InvokeFun
        ) {
            // target不是Application/Activity/Fragment就不支持
            checkSupport(target.javaClass)
            // 附着 Lifecycle
            AttachLifecycle.attachLifecycle(target) {
                // Lifecycle 附着完成，在子线程修改缓存字典
                ThreadExecutor.execute { insertInvoke(target, isInBg, invoke) }
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
            ) { _, method, _ ->
                val methodName = method?.name ?: ""
                if (methodName == "onDestroy") {
                    // 不等线程阻塞修改invoke缓存
                    invokes[key]?.isActive = false
                    bgInvokes[key]?.isActive = false
                    invokes[key]?.invokes = arrayListOf()
                    bgInvokes[key]?.invokes = arrayListOf()
                    // 子线程修改缓存字典
                    ThreadExecutor.execute { removeNormalInvoke(key) }
                }
                methodName
            } as Lifecycle
        }

        @GuardedBy("Register.class")
        private fun <T : Any> insertInvoke(
            target: T, isInBg: Boolean, invoke: InvokeFun
        ) {
            val key = target.hashCode()
            if (!isInBg) {
                if (invokes[key] == null) {
                    invokes[key] = InvokeWrapper()
                    invokes[key]!!.invokes.add(invoke)
                } else {
                    if (!invokes[key]!!.invokes.contains(invoke) && invokes[key]!!.isActive)
                        invokes[key]!!.invokes.add(invoke)
                }
            } else {
                if (bgInvokes[key] == null) {
                    bgInvokes[key] = InvokeWrapper()
                    bgInvokes[key]!!.invokes.add(invoke)
                } else {
                    if (!bgInvokes[key]!!.invokes.contains(invoke) && bgInvokes[key]!!.isActive)
                        bgInvokes[key]!!.invokes.add(invoke)
                }
            }
        }

        @GuardedBy("Register.class")
        private fun removeNormalInvoke(key: Int) {
            invokes.remove(key)
            bgInvokes.remove(key)
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
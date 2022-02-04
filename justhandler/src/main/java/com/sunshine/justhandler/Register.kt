package com.sunshine.justhandler

import android.app.Application
import android.app.Service
import androidx.annotation.GuardedBy
import com.sunshine.justhandler.excutor.ThreadExecutor
import com.sunshine.justhandler.invoke.InvokeFun
import com.sunshine.justhandler.invoke.InvokeWrapper
import com.sunshine.justhandler.lifecycle.AttachLifecycle
import com.sunshine.justhandler.lifecycle.Lifecycle
import java.lang.IllegalArgumentException
import java.lang.reflect.Proxy

/**
 * created by: Sunshine at 2021/11/23
 * desc: 信息回调注册器
 */
internal class Register {
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
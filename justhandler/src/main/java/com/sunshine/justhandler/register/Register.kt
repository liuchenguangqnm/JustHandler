package com.sunshine.justhandler.register

import android.app.Application
import android.app.Service
import androidx.annotation.GuardedBy
import com.sunshine.justhandler.ipc.IPCFunction
import com.sunshine.justhandler.excutor.ThreadExecutor
import com.sunshine.justhandler.invoke.InvokeFun
import com.sunshine.justhandler.invoke.InvokeThreadType
import com.sunshine.justhandler.invoke.InvokeWrapper
import com.sunshine.justhandler.lifecycle.AttachLifecycle
import com.sunshine.justhandler.lifecycle.Lifecycle
import java.lang.IllegalArgumentException
import java.lang.reflect.Proxy
import java.util.*
import kotlin.collections.HashMap

/**
 * created by: Sunshine at 2021/11/23
 * desc: 信息回调注册器
 */
internal class Register {
    companion object {
        // invokeWrapper表单（与生命周期挂钩）
        val invokeWrappers = HashMap<Int, InvokeWrapper>()
        val threadInvokeWrappers = HashMap<Int, InvokeWrapper>()

        // invokeWrapper缓存中间件
        val invokeLifecycles = HashMap<String, LinkedList<Int>>()
        val threadInvokeLifecycles = HashMap<String, LinkedList<Int>>()

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
                    invokeWrappers[key]?.isActive = false
                    threadInvokeWrappers[key]?.isActive = false
                    // 子线程修改缓存字典
                    ThreadExecutor.execute { removeInvoke(key) }
                } else if (methodName == "onDestroyToMsgTag") {
                    // 遍历方法参数取出当前组件希望取消监听的msgTag
                    args?.map { arg ->
                        if (arg is Array<*>) {
                            arg.map { argItem ->
                                // 子线程修改缓存字典
                                if (argItem is String) ThreadExecutor.execute {
                                    removeInvoke(key, argItem)
                                }
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
            if (invoke.invokeThread != InvokeThreadType.SEND_THREAD) {
                // invokeWrapper生成或修改缓存字典
                if (invokeWrappers[key] == null) {
                    invokeWrappers[key] = InvokeWrapper()
                    invokeWrappers[key]?.addInvoke(invoke)
                } else if (invokeWrappers[key]?.isActive == true) {
                    invokeWrappers[key]?.addInvoke(invoke)
                }
                // 中间件生成或修改缓存字典
                if (invokeLifecycles[invoke.msgTag] == null)
                    invokeLifecycles[invoke.msgTag] = LinkedList<Int>()
                if (invokeLifecycles[invoke.msgTag]?.contains(key) == false)
                    invokeLifecycles[invoke.msgTag]?.add(key)
            } else {
                // invokeWrapper生成或修改缓存字典
                if (threadInvokeWrappers[key] == null) {
                    threadInvokeWrappers[key] = InvokeWrapper()
                    threadInvokeWrappers[key]?.addInvoke(invoke)
                } else if (threadInvokeWrappers[key]?.isActive == true) {
                    threadInvokeWrappers[key]?.addInvoke(invoke)
                }
                // 中间件生成或修改缓存字典
                if (threadInvokeLifecycles[invoke.msgTag] == null)
                    threadInvokeLifecycles[invoke.msgTag] = LinkedList<Int>()
                if (threadInvokeLifecycles[invoke.msgTag]?.contains(key) == false)
                    threadInvokeLifecycles[invoke.msgTag]?.add(key)
            }
            // 尝试注册IDE通信
            IPCFunction.ideRegister()
        }

        @GuardedBy("Register.class")
        private fun removeInvoke(key: Int, msgTag: String) {
            // invokeWrapper删除对应注册
            val invokeSize = invokeWrappers[key]?.removeInvoke(msgTag)
            val threadInvokeSize = threadInvokeWrappers[key]?.removeInvoke(msgTag)
            // 中间件删除对应缓存
            if (invokeSize == 0) {
                invokeLifecycles[msgTag]?.remove(key)
                if (invokeLifecycles[msgTag]?.size == 0) invokeLifecycles.remove(msgTag)
            }
            if (threadInvokeSize == 0) {
                threadInvokeLifecycles[msgTag]?.remove(key)
                if (threadInvokeLifecycles[msgTag]?.size == 0) invokeLifecycles.remove(msgTag)
            }
            // 尝试取消注册IDE通信
            IPCFunction.ideUnRegister()
        }

        @GuardedBy("Register.class")
        private fun removeInvoke(key: Int) {
            // invokeWrapper删除对应注册
            val invokeWrapper = invokeWrappers.remove(key)
            val threadInvokeWrapper = threadInvokeWrappers.remove(key)
            // 中间件删除对应缓存
            invokeWrapper?.getInvokes()?.map {
                invokeLifecycles.remove(it.msgTag)
            }
            threadInvokeWrapper?.getInvokes()?.map {
                threadInvokeLifecycles.remove(it.msgTag)
            }
            // 尝试取消注册IDE通信
            IPCFunction.ideUnRegister()
        }

        // 是否是支持的参数类型
        private fun checkSupport(targetClass: Class<Any>) {
            val unSupport = Application::class.java.isAssignableFrom(targetClass) ||
                    Service::class.java.isAssignableFrom(targetClass)
            if (unSupport) {
                throw IllegalArgumentException("target 不支持 Application/Service")
            }
        }
    }
}
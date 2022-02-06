package com.sunshine.justhandler

import com.sunshine.justhandler.invoke.InvokeFun
import com.sunshine.justhandler.lifecycle.Lifecycle
import com.sunshine.justhandler.register.Register
import com.sunshine.justhandler.sender.MessageSender


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
            MessageSender.sendMessage(msgTag, data, post)
            return Companion
        }

        /**
         * 响应消息
         * @param lifecycleTarget 非 Application/Service 对象
         * @param invoke          信息回调 Function
         */
        @JvmStatic
        fun getMsg(lifecycleTarget: Any, invoke: InvokeFun) {
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


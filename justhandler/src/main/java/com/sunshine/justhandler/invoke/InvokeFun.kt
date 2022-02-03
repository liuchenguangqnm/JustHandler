package com.sunshine.justhandler.invoke

/**
 * created by: Sunshine at 2021/11/23
 * desc: 消息回调 Function
 * @param msgTag 消息甄别 messageTag 队列
 */
abstract class InvokeFun(val msgTag: String, val invokeThread: InvokeThreadType) {
    abstract fun invoke(obj: Any?)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InvokeFun) return false

        if (this.msgTag != other.msgTag) return false
        if (this.invokeThread != other.invokeThread) return false

        return true
    }

    override fun hashCode(): Int {
        return msgTag.hashCode()
    }

    override fun toString(): String {
        return "InvokeFun(msgTag='$msgTag')"
    }
}

/**
 * 消息响应线程类型：
 * MAIN_THREAD：在UI线程响应
 * SEND_THREAD：在发送消息的线程中响应（如果发送消息的线程是非UI线程，则接收方的响应时机必然在该线程的代码执行结束之后）
 * RANDOM_THREAD：在任意的非UI线程中想用
 */
enum class InvokeThreadType(val type: Int) {
    MAIN_THREAD(1) {
        fun getTypeValue(): Int {
            return type
        }
    },
    SEND_THREAD(2) {
        fun getTypeValue(): Int {
            return type
        }
    },
    RANDOM_THREAD(3) {
        fun getTypeValue(): Int {
            return type
        }
    }
}
package com.example.justhandler.justhandler.invoke

/**
 * created by: Sunshine at 2021/11/23
 * desc: 消息回调 Function
 * @param msgTag 消息甄别 messageTag 队列
 */
abstract class InvokeFun(val msgTag: String) {
    abstract fun invoke(obj: Any?)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InvokeFun) return false

        if (this.msgTag != other.msgTag) return false

        return true
    }

    override fun hashCode(): Int {
        return msgTag.hashCode()
    }

    override fun toString(): String {
        return "InvokeFun(msgTag='$msgTag')"
    }
}
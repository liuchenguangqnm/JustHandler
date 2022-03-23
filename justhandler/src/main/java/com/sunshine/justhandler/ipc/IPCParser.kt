package com.sunshine.justhandler.ipc

import com.sunshine.justhandler.ipc.Serializer.Companion.getDataSerialize

/**
 * created by: Sunshine at 2022/2/25
 * desc: 进程间通信包解析
 */
internal class IPCParser {
    companion object {
        fun antiSerialize(
            json: String, currentProcessName: String,
            invoke: (msgTag: String, msgData: Any?, post: Long) -> Unit
        ) {
            val data = AntiSerializer.getData(json)
        }

        fun serialize(
            fromProcess: String, msgTag: String, msgData: Any?, msgLong: Long
        ): String? {
            val serializeMsgData = getDataSerialize(msgData)
            val wrapper = IPCWrapper(
                fromProcess, msgTag, serializeMsgData, msgLong
            )
            return getDataSerialize(wrapper)
        }
    }
}

// 进程间通信包
private data class IPCWrapper(
    val fromProcess: String,
    val msgTag: String,
    val msgData: String?,
    val msgLong: Long?
)
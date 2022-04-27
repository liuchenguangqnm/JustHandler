package com.sunshine.justhandler.ipc

import android.util.Log
import com.sunshine.justhandler.ipc.serializer.AntiSerializer
import com.sunshine.justhandler.ipc.serializer.Serializer.Companion.getDataSerialize

/**
 * created by: Sunshine at 2022/2/25
 * desc: 进程间通信包解析
 */
internal class IPCParser {
    companion object {
        inline fun antiSerialize(
            wrapperJson: String, currentProcessName: String,
            invoke: (msgTag: String, msgData: Any?, post: Long) -> Unit
        ) {
            val iPCWrapper = AntiSerializer.parseJson(wrapperJson)
            Log.i("haha-1", getDataSerialize(wrapperJson) ?: "null")
            if (iPCWrapper is IPCWrapper) {
                if (currentProcessName == iPCWrapper.fromProcess) return
                val dataJson = iPCWrapper.msgData
                if (dataJson.isNullOrEmpty()) return
                // Log.i("haha-0", dataJson)
//                val data = AntiSerializer.parseJson(dataJson)
                // Log.i("haha-1", getDataSerialize(data) ?: "null")
            }
        }

        fun serialize(
            fromProcess: String, msgTag: String, msgData: Any?, msgLong: Long
        ): String? {
            val serializeMsgData = getDataSerialize(msgData)
            val wrapper = IPCWrapper(fromProcess, msgTag, serializeMsgData, msgLong)
            return getDataSerialize(wrapper)
        }
    }
}

// 进程间通信包
internal data class IPCWrapper(
    val fromProcess: String,
    val msgTag: String,
    val msgData: String?,
    val msgLong: Long?
)
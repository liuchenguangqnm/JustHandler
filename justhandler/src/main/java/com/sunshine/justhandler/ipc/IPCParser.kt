package com.sunshine.justhandler.ipc

import android.util.Log
import com.sunshine.justhandler.ipc.serializer.AntiSerializer
import com.sunshine.justhandler.ipc.serializer.Serializer.Companion.getDataSerialize
import org.json.JSONObject

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
            Log.i("haha-0", wrapperJson)

            // 前置判断
            val iPCWrapperJSONObj = JSONObject(wrapperJson).optJSONObject("data") ?: return
            val fromProcess = iPCWrapperJSONObj.optString("fromProcess")
            if (currentProcessName == fromProcess) return
            // 关键参数获取
            val msgTag = iPCWrapperJSONObj.optString("msgTag")
            val msgData = iPCWrapperJSONObj.optString("msgData")
            val dataType = iPCWrapperJSONObj.optString("dataType")
            val msgLong = iPCWrapperJSONObj.optLong("msgLong")

            val data = AntiSerializer.parseJson(msgData, dataType)
            Log.i("haha-1", getDataSerialize(data) ?: "null")
        }

        fun serialize(
            fromProcess: String, msgTag: String, msgData: Any?, msgLong: Long
        ): String? {
            val serializeMsgData = getDataSerialize(msgData)
            val dataType = msgData?.javaClass?.canonicalName ?: ""
            val wrapper = IPCWrapper(fromProcess, msgTag, serializeMsgData, dataType, msgLong)
            return getDataSerialize(wrapper)
        }
    }
}

// 进程间通信包
internal data class IPCWrapper(
    val fromProcess: String,
    val msgTag: String,
    val msgData: String?,
    val dataType: String,
    val msgLong: Long?
)
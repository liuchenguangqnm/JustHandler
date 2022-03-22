package com.sunshine.justhandler.ipc

import android.util.Log
import com.sunshine.justhandler.ipc.Serializer.Companion.getDataSerialize

/**
 * created by: Sunshine at 2022/2/25
 * desc: 进程间通信包解析
 */
internal class IPCParser {
    companion object {
        fun unSerialize(
            json: String, currentProcessName: String,
            invoke: (msgTag: String, msgData: Any?, post: Long) -> Unit
        ) {

            Log.i("haha", json)

//            try {
//                val clazz = Class.forName("com.example.justhandler.testBean.MsgBean")
//                Log.i("haha0", "=============${clazz.canonicalName}")
//                val field = clazz.getDeclaredField("tag")
//                field.isAccessible = true
//                Log.i("haha1", field.name)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
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
package com.sunshine.justhandler.ipc

import android.util.Log
import com.sunshine.justhandler.ipc.Serializer.Companion.getDataSerialize
import java.lang.Exception

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
            try {
                val clazz = Class.forName("com.example.justhandler.testBean.MsgBean")
                clazz.declaredConstructors.forEach {
                    it.isAccessible = true
                    for (parameterType in it.parameterTypes) {
                        Log.i("haha", parameterType.canonicalName ?: "null")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

//            Log.i("haha", json)
//            try {
//                val ipcWrapper = gson.fromJson(json, IPCWrapper::class.java)
//                if (ipcWrapper.msgTag.isEmpty()) return
//                if (currentProcessName == ipcWrapper.fromProcess) return
//                if (ipcWrapper.msgCanonical.isEmpty()) {
//                    invoke.invoke(ipcWrapper.msgTag, null, ipcWrapper.msgLong ?: 0)
//                } else {
//                    val data = gson.fromJson(
//                        ipcWrapper.msgData, Class.forName(ipcWrapper.msgCanonical)
//                    )
//                    invoke.invoke(ipcWrapper.msgTag, data, ipcWrapper.msgLong ?: 0)
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
        }

        fun serialize(
            fromProcess: String, msgTag: String, msgData: Any?, msgLong: Long
        ): String? {
            val msgCanonical = msgData?.javaClass?.canonicalName ?: ""
            val serializeMsgData = getDataSerialize(msgData)
            val wrapper = IPCWrapper(
                fromProcess, msgTag, msgCanonical, serializeMsgData, msgLong
            )
            return getDataSerialize(wrapper)
        }
    }
}

// 进程间通信包
private data class IPCWrapper(
    val fromProcess: String,
    val msgTag: String,
    val msgCanonical: String,
    val msgData: Any?,
    val msgLong: Long?
)
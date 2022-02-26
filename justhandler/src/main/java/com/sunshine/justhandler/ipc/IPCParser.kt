package com.sunshine.justhandler.ipc

import com.google.gson.Gson
import java.lang.Exception

/**
 * created by: Sunshine at 2022/2/25
 * desc: 进程间通信包解析
 */
internal class IPCParser {
    companion object {
        private val gson = Gson()

        fun getData(
            json: String, currentProcessName: String,
            invoke: (msgTag: String, msgData: Any?, post: Long) -> Unit
        ) {
            try {
                val ipcWrapper = gson.fromJson(json, IPCWrapper::class.java)
                if (ipcWrapper.msgTag.isEmpty()) return
                if (currentProcessName == ipcWrapper.fromProcess) return
                if (ipcWrapper.msgCanonical.isEmpty()) {
                    invoke.invoke(ipcWrapper.msgTag, null, ipcWrapper.msgLong ?: 0)
                } else {
                    val data = gson.fromJson(
                        ipcWrapper.msgData, Class.forName(ipcWrapper.msgCanonical)
                    )
                    invoke.invoke(ipcWrapper.msgTag, data, ipcWrapper.msgLong ?: 0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun getJson(
            fromProcess: String, msgTag: String, msgData: Any?, msgLong: Long
        ): String {
            val msgCanonical = msgData?.javaClass?.canonicalName ?: ""
            val wrapperMsgData = if (msgCanonical.isNotEmpty()) gson.toJson(msgData) else null
            val wrapper = IPCWrapper(
                fromProcess, msgTag, msgCanonical, wrapperMsgData, msgLong
            )
            return gson.toJson(wrapper)
        }
    }
}


/**
 * created by: Sunshine at 2022/2/25
 * desc: 进程间通信包
 */
private data class IPCWrapper(
    val fromProcess: String,
    val msgTag: String,
    val msgCanonical: String,
    val msgData: String?,
    val msgLong: Long?
)
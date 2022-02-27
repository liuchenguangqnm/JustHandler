package com.sunshine.justhandler.ipc

import android.util.Log
import java.lang.Exception
import java.util.*

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

        private fun getDataSerialize(data: Any?): String? {
            if (data == null) return null
            when {
                data is String -> return "\"${data.javaClass.canonicalName}_${data}\""
                data is Int -> return "\"${data.javaClass.canonicalName}_${data}\""
                data is Long -> return "\"${data.javaClass.canonicalName}_${data}\""
                data is Float -> return "\"${data.javaClass.canonicalName}_${data}\""
                data is Double -> return "\"${data.javaClass.canonicalName}_${data}\""
                data::class.java.isArray -> {
                    return getListSerialize((data as Array<*>).toList())
                }
                else -> {
                    // 首先判断是不是字典或队列
                    for (interFace in data::class.java.interfaces) {
                        if ("java.util.List" == interFace.canonicalName) {
                            return getListSerialize(ArrayList((data as List<*>)))
                        }
                        if ("java.util.Map" == interFace.canonicalName) {
                            return getMapSerialize(data as Map<*, *>)
                        }
                    }
                    // 其次再尝试做普通对象的序列化
                    val loader = data.javaClass.classLoader?.javaClass?.canonicalName
                    return if (loader == null || loader == "java.lang.BootClassLoader") {
                        "{\"data\":{},\"type\":\"${data.javaClass.canonicalName}\"}"
                    } else getAnySerialize(data)
                }
            }
        }

        private fun getListSerialize(list: List<*>): String {
            val strBuf = StringBuffer("{\"list\":[")
            for (index in list.indices) {
                // node
                val indexValue = list[index]
                val value = when {
                    indexValue is String -> "\"${String::class.java.canonicalName}_${indexValue}\""
                    indexValue != null -> getDataSerialize(indexValue)
                    else -> null
                }
                if (value != null) strBuf.append("$value,")
            }
            if (strBuf.endsWith(",")) strBuf.delete(strBuf.length - 1, strBuf.length)
            strBuf.append("],\"type\":\"${list.javaClass.canonicalName}\"}")
            return strBuf.toString()
        }

        private fun getMapSerialize(map: Map<*, *>): String {
            val strBuf = StringBuffer("{\"map\":{")
            map.entries.forEach {
                // key
                val key = when {
                    it.key is String -> "\"${String::class.java.canonicalName}_${it.key}\""
                    it.key != null -> getDataSerialize(it.key)
                    else -> null
                }
                // value
                val value = when {
                    it.value is String -> "\"${String::class.java.canonicalName}_${it.value}\""
                    it.value != null -> getDataSerialize(it.value)
                    else -> null
                }
                if (!key.isNullOrEmpty()) strBuf.append("$key:$value,")
            }
            if (strBuf.endsWith(",")) strBuf.delete(strBuf.length - 1, strBuf.length)
            strBuf.append("},\"type\":\"${map.javaClass.canonicalName}\"}")
            return strBuf.toString()
        }

        private fun getAnySerialize(data: Any): String {
            val strBuf = StringBuffer("{\"data\":{")
            try {
                val fields = data::class.java.declaredFields
                for (f in fields) {
                    f.isAccessible = true
                    // if (Modifier.toString(f.modifiers).contains("private")) continue
                    val fValue = f.get(data)
                    val fData = when {
                        fValue is String -> {
                            if (fValue.startsWith("{") && fValue.endsWith("}")) {
                                fValue
                            } else if (fValue.startsWith("[") && fValue.endsWith("]")) {
                                fValue
                            } else "\"${String::class.java.canonicalName}_${fValue}\""
                        }
                        fValue != null -> getDataSerialize(fValue)
                        else -> null
                    }
                    strBuf.append("\"${f.name}\":$fData,")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (strBuf.endsWith(",")) strBuf.delete(strBuf.length - 1, strBuf.length)
            strBuf.append("},\"type\":\"${data.javaClass.canonicalName}\"}")
            return strBuf.toString()
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
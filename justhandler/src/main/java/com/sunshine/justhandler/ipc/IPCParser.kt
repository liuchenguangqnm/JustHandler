package com.sunshine.justhandler.ipc

import org.json.JSONException
import org.json.JSONObject

/**
 * created by: Sunshine at 2022/2/25
 * desc: 进程间通信数据解析
 */
internal class IPCParser {
    companion object {
        fun parseJson(json: String): Any? {
            try {
                val jsonObject = JSONObject(json)
                return null
            } catch (e: JSONException) {
                return null
            }
        }

        fun getJson(data: Any?): Any? {
            if (data == null) return null
            return when (data) {
                is String -> data
                is Int -> data
                is Long -> data
                is Float -> data
                is Double -> data
                else -> {
                    val strBuf = StringBuffer("{")
                    try {
                        val fields = data::class.java.declaredFields
                        for (f in fields) {
                            f.isAccessible = true
                            // if (Modifier.toString(f.modifiers).contains("private")) continue
                            val fData = if (f.get(data) is String) "\"${f.get(data)}\""
                            else getJson(f.get(data))
                            strBuf.append("\"${f.name}\":$fData")
                            strBuf.append(",")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (strBuf.length > 1) strBuf.delete(strBuf.length - 1, strBuf.length)
                    strBuf.append("}")
                    strBuf.toString()
                }
            }
        }

//        is Array<*> -> {
//            val strBuf = StringBuffer("[")
//            for (index in 0 until data.size) {
//                if (index > 0) strBuf.append(",")
//                val value = if (data[index] is String) "\"${data[index]}\""
//                else getJson(data[index])
//                strBuf.append(value)
//            }
//            strBuf.append("]")
//            strBuf.toString()
//        }
//        is List<*> -> {
//            val strBuf = StringBuffer("[")
//            for (index in 0 until data.size) {
//                if (index > 0) strBuf.append(",")
//                val value = if (data[index] is String) "\"${data[index]}\""
//                else getJson(data[index])
//                strBuf.append(value)
//            }
//            strBuf.append("]")
//            strBuf.toString()
//        }
//        is Map<*, *> -> {
//            val strBuf = StringBuffer("{")
//            data.entries.forEach {
//                // key
//                val key = if (it.key == null) ""
//                else "\"${it.key}\""
//                // value
//                val value = if (it.value is String) "\"${it.value}\""
//                else getJson(it.value)
//                if (key.isNotEmpty()) strBuf.append("$key:$value")
//            }
//            if (strBuf.length > 1) strBuf.delete(strBuf.length - 1, strBuf.length)
//            strBuf.append("}")
//            strBuf.toString()
//        }
    }
}
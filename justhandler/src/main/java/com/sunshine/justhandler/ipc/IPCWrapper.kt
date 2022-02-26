package com.sunshine.justhandler.ipc

import android.util.Log
import java.lang.reflect.ParameterizedType

/**
 * created by: Sunshine at 2022/2/25
 * desc: 进程间通信包
 */
data class IPCWrapper(
    val fromProcess: String,
    val msgTag: String,
    val msgCanonical: String,
    val msgData: Any?,
    val msgLong: Long
) {
    private var listInfo: ListInfo? = null
    private var mapInfo: MapInfo? = null

    init {
        if (msgData is Array<*> || msgData is Collection<*>) {
            val genericSuperclass = msgData.javaClass.genericSuperclass
            if (genericSuperclass is ParameterizedType) {
                val type = genericSuperclass.actualTypeArguments[0]
                if (type is Class<*>) {
                    Log.i("haha", type.canonicalName ?: "")
                }
            }
        } else if (msgData is Map<*, *>) {
            val genericSuperclass = msgData.javaClass.genericSuperclass
            if (genericSuperclass is ParameterizedType) {
                val keyType = genericSuperclass.actualTypeArguments[0]
                val valueType = genericSuperclass.actualTypeArguments[0]
                if (keyType is Class<*>) {
                    Log.i("haha0", keyType.canonicalName ?: "")
                }
                if (valueType is Class<*>) {
                    Log.i("haha1", valueType.canonicalName ?: "")
                }
            }
        }
    }

    fun getListInfo(): ListInfo? {
        return listInfo
    }

    fun getMapInfo(): MapInfo? {
        return mapInfo
    }

    data class ListInfo(
        val dataType: List<String>
    )

    data class MapInfo(
        val keyType: String,
        val valueType: List<String>
    )
}
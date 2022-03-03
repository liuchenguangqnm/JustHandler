package com.sunshine.justhandler.ipc

import java.lang.Exception
import java.lang.reflect.Field
import java.util.*

/**
 * created by: Sunshine at 2022/2/27
 * desc: 序列化工具
 */
internal class Serializer {
    companion object {
        fun getDataSerialize(data: Any?): String? {
            if (data == null) return null
            when (data) {
                is String -> return "\"${data.javaClass.canonicalName}*${data}\""
                is Int -> return "\"${data.javaClass.canonicalName}*${data}\""
                is Long -> return "\"${data.javaClass.canonicalName}*${data}\""
                is Float -> return "\"${data.javaClass.canonicalName}*${data}\""
                is Double -> return "\"${data.javaClass.canonicalName}*${data}\""
                is Array<*> -> return getListSerialize(data.toList())
                else -> {
                    // 首先判断是不是字典或队列
                    for (interFace in getClasses(data.javaClass)) {
                        if ("java.util.List" == interFace.canonicalName) {
                            return getListSerialize(ArrayList((data as List<*>)))
                        }
                        if ("java.util.Map" == interFace.canonicalName) {
                            return getMapSerialize(data as Map<*, *>)
                        }
                    }
                    // 其次再尝试做普通对象的序列化
                    val loader = data.javaClass.classLoader?.javaClass?.canonicalName
                        ?: "{\"data\":{},\"type\":\"${data.javaClass.canonicalName}\"}"
                    return if (loader == "java.lang.BootClassLoader") {
                        getAnySerialize(data, true)
                    } else getAnySerialize(data, false)
                }
            }
        }

        private fun getListSerialize(list: List<*>): String {
            val strBuf = StringBuffer("{\"list\":[")
            for (index in list.indices) {
                // node
                val indexValue = list[index]
                val value = when {
                    indexValue is String -> "\"${String::class.java.canonicalName}*${indexValue}\""
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
                    it.key is String -> "\"${String::class.java.canonicalName}*${it.key}\""
                    it.key != null -> getDataSerialize(it.key)
                    else -> null
                }
                // value
                val value = when {
                    it.value is String -> "\"${String::class.java.canonicalName}*${it.value}\""
                    it.value != null -> getDataSerialize(it.value)
                    else -> null
                }
                if (!key.isNullOrEmpty()) strBuf.append("$key:$value,")
            }
            if (strBuf.endsWith(",")) strBuf.delete(strBuf.length - 1, strBuf.length)
            strBuf.append("},\"type\":\"${map.javaClass.canonicalName}\"}")
            return strBuf.toString()
        }


        private fun getAnySerialize(data: Any, isBootLoader: Boolean): String {
            val strBuf = StringBuffer("{\"data\":{")
            try {
                val fields = getFields(data)
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
                            } else {
                                "\"${String::class.java.canonicalName}*${fValue}\""
                            }
                        }
                        fValue != null -> {
                            if (isBootLoader) getBootSerialize(fValue)
                            else getDataSerialize(fValue)
                        }
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

        private fun getBootSerialize(data: Any): String {
            when (data) {
                is String -> return "\"${data.javaClass.canonicalName}*${data}\""
                is Int -> return "\"${data.javaClass.canonicalName}*${data}\""
                is Long -> return "\"${data.javaClass.canonicalName}*${data}\""
                is Float -> return "\"${data.javaClass.canonicalName}*${data}\""
                is Double -> return "\"${data.javaClass.canonicalName}*${data}\""
                is Array<*> -> return "{\"list\":[],\"type\":\"${data.javaClass.canonicalName}\"}"
                else -> {
                    // 首先判断是不是字典或队列
                    for (interFace in getClasses(data.javaClass)) {
                        if ("java.util.List" == interFace.canonicalName)
                            return "{\"list\":[],\"type\":\"${data.javaClass.canonicalName}\"}"
                        if ("java.util.Map" == interFace.canonicalName)
                            return "{\"map\":{},\"type\":\"${data.javaClass.canonicalName}\"}"
                    }
                    // 否则返回普通对象的Json串
                    return "{\"data\":{},\"type\":\"${data.javaClass.canonicalName}\"}"
                }
            }
        }

        private fun getFields(data: Any): LinkedList<Field> {
            // 初始化结果
            val result = LinkedList<Field>()
            // 初始化排重map并填充
            val map = LinkedHashMap<String, Field>()
            val classes = getClasses(data.javaClass)
            classes.forEach { clazz ->
                clazz.declaredFields.forEach { field ->
                    if (isAdd(field, map)) map[field.name] = field
                }
            }
            // 返回结果填充
            map.forEach { result.add(it.value) }
            return result
        }

        private fun getClasses(
            dataClass: Class<*>, input: LinkedList<Class<*>> = LinkedList()
        ): List<Class<*>> {
            // 添加参数class
            input.add(dataClass)
            dataClass.interfaces.forEach { input.add(it) }
            // 获取父类class
            val superclass: Class<*> = dataClass.superclass ?: return input
            getClasses(superclass, input)
            // 结果
            return input
        }

        private fun isAdd(field: Field, map: LinkedHashMap<String, Field>): Boolean {
            return !(field.name == "shadow\$_klass_" ||
                    field.name == "shadow\$_monitor_" ||
                    map.containsKey(field.name))
        }
    }
}
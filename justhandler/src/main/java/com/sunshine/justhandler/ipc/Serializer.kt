package com.sunshine.justhandler.ipc

import java.lang.Exception
import java.lang.reflect.Field
import java.lang.reflect.Modifier
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
                is String -> return "\"${data}\""
                is Int -> return "$data"
                is Long -> return "$data"
                is Float -> return "$data"
                is Double -> return "$data"
                is Boolean -> return "$data"
                is Array<*> -> return getListSerialize(data.toList())
                else -> {
                    // 首先判断是不是字典或队列
                    if (java.util.Collection::class.java.isAssignableFrom(data.javaClass)) {
                        return getListSerialize(ArrayList((data as List<*>)))
                    } else if (java.util.Map::class.java.isAssignableFrom(data.javaClass)) {
                        return getMapSerialize(data as Map<*, *>)
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
                    indexValue is String -> "\"$indexValue\""
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
                    it.key is String -> "\"${it.key}\""
                    it.key != null -> {
                        "\"${(it.key)!!.javaClass.canonicalName}*${getDataSerialize(it.key)}\""
                    }
                    else -> null
                }
                // value
                val value = when {
                    it.value is String -> "\"${it.value}\""
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
                    val fValue = f.get(data)
                    val fData = when {
                        fValue is String -> {
                            if (fValue.startsWith("{") && fValue.endsWith("}")) {
                                fValue
                            } else if (fValue.startsWith("[") && fValue.endsWith("]")) {
                                fValue
                            } else {
                                "\"${fValue}\""
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
                is String -> return "\"${data}\""
                is Int -> return "$data"
                is Long -> return "$data"
                is Float -> return "$data"
                is Double -> return "$data"
                is Boolean -> return "$data"
                is Array<*> -> return "{\"list\":[],\"type\":\"${data.javaClass.canonicalName}\"}"
                else -> {
                    // 首先判断是不是字典或队列
                    if (java.util.Collection::class.java.isAssignableFrom(data.javaClass)) {
                        return "{\"list\":[],\"type\":\"${data.javaClass.canonicalName}\"}"
                    } else if (java.util.Map::class.java.isAssignableFrom(data.javaClass)) {
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
            val modifiers = Modifier.toString(field.modifiers)
            return !(modifiers.contains("transient")
                    || modifiers.contains("static")
                    || map.containsKey(field.name))
        }
    }
}
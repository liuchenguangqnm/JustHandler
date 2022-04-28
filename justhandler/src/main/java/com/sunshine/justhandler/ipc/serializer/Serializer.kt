package com.sunshine.justhandler.ipc.serializer

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
        fun getDataSerialize(data: Any?, isListOrDicElement: Boolean? = false): String? {
            if (data == null) return null
            when (data) {
                is String -> {
                    return if (isListOrDicElement != true) "$data"
                    else "\"${data.javaClass.canonicalName}*${data}\""
                }
                is Int -> {
                    return if (isListOrDicElement != true) "$data"
                    else "\"${data.javaClass.canonicalName}*${data}\""
                }
                is Long -> {
                    return if (isListOrDicElement != true) "$data"
                    else "\"${data.javaClass.canonicalName}*${data}\""
                }
                is Float -> {
                    return if (isListOrDicElement != true) "$data"
                    else "\"${data.javaClass.canonicalName}*${data}\""
                }
                is Double -> {
                    return if (isListOrDicElement != true) "$data"
                    else "\"${data.javaClass.canonicalName}*${data}\""
                }
                is Boolean -> {
                    return if (isListOrDicElement != true) "$data"
                    else "\"${data.javaClass.canonicalName}*${data}\""
                }
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

        // 获取list或数组对象的Json
        private fun getListSerialize(list: List<*>): String {
            val strBuf = StringBuffer("{\"list\":[")
            for (index in list.indices) {
                // node
                val indexValue = list[index]
                val value = getDataSerialize(indexValue, true)
                if (value != null) strBuf.append("$value,")
            }
            if (strBuf.endsWith(",")) strBuf.delete(strBuf.length - 1, strBuf.length)
            strBuf.append("],\"type\":\"${list.javaClass.canonicalName}\"}")
            return strBuf.toString()
        }

        // 获取map对象的Json
        private fun getMapSerialize(map: Map<*, *>): String {
            val strBuf = StringBuffer("{\"map\":{")
            map.entries.forEach {
                // key
                val key = when {
                    it.key is String -> "\"${(it.key)!!.javaClass.canonicalName}*${it.key}\""
                    it.key != null -> {
                        // 如果key是一个非基础数据类型和字符串的对象，则将对象转为json串
                        var dataJson = getDataSerialize(it.key)
                        dataJson = dataJson?.replace("\"", "\\\"") ?: "null" // json字符串的引号要加斜杠
                        "\"${(it.key)!!.javaClass.canonicalName}*${dataJson}\""
                    }
                    else -> "null"
                }
                // value
                val value = getDataSerialize(it.value, true)
                strBuf.append("$key:$value,")
            }
            if (strBuf.endsWith(",")) strBuf.delete(strBuf.length - 1, strBuf.length)
            strBuf.append("},\"type\":\"${map.javaClass.canonicalName}\"}")
            return strBuf.toString()
        }

        // 获取普通对象的Json
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

        // 获取官方类对象的Json（抛弃内部对象型成员的解析，以免方法栈溢出Gson也用了类似的套路）
        private fun getBootSerialize(data: Any): String {
            when (data) {
                is String -> return "$data"
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
package com.sunshine.justhandler.ipc.serializer

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.*
import kotlin.collections.LinkedHashMap

/**
 * created by: Sunshine at 2022/2/27
 * desc: 反序列化工具
 */
class AntiSerializer {
    companion object {
        fun parseJson(json: String, jsonType: String): Any? {
            when (jsonType) {
                // 基础数据类型
                java.lang.String::class.java.canonicalName -> return json
                java.lang.Integer::class.java.canonicalName -> return json.toInt()
                java.lang.Long::class.java.canonicalName -> return json.toLong()
                java.lang.Float::class.java.canonicalName -> return json.toFloat()
                java.lang.Double::class.java.canonicalName -> return json.toDouble()
                java.lang.Boolean::class.java.canonicalName -> return json.toBoolean()
                // 其它数据类型
                else -> {
                    // 首先初始化默认返回结果
                    var result: Any? = null
                    // 其次构建Json对象，并获取内部的常用成员变量
                    val jsonObj = try {
                        JSONObject(json)
                    } catch (e: JSONException) {
                        return json
                    }
                    val type = jsonObj.optString("type")
                    val list = jsonObj.optJSONArray("list")
                    val map = jsonObj.optJSONObject("map")
                    val data = jsonObj.optJSONObject("data")
                    // 通过Type得到Json数据对应的class类型
                    if (type.isNullOrEmpty()) return result
                    // 序列化得到数据Bean
                    try {
                        when {
                            type.endsWith("[]") -> {
                                result = if (list != null && jsonType.length > 2) {
                                    getInstance(list, Array<Any?>::class.java)
                                } else null
                            }
                            data != null -> result = getInstance(data, Class.forName(type))
                            map != null -> result = getInstance(map, Class.forName(type))
                            list != null -> result = getInstance(list, Class.forName(type))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return result
                }
            }
        }

        private fun getInstance(data: JSONArray, clazz: Class<*>): Any? {
            return when {
                clazz.isArray -> {
                    val array = arrayOfNulls<Any?>(data.length())
                    for (index in 0 until data.length()) {
                        val indexData = data[index]?.toString() ?: ""
                        val typeIndex = indexData.indexOfFirst { "*" == it.toString() }
                        if (typeIndex <= 0 || typeIndex == indexData.length) {
                            if (data[index] is JSONObject) {
                                val dataType = (data[index] as JSONObject).optString("type")
                                if (dataType.isEmpty()) array[index] = null
                                else array[index] = parseJson(indexData, dataType)
                            } else array[index] = null
                        } else {
                            val dataType = indexData.substring(0, typeIndex)
                            val dataJson = indexData.substring(typeIndex + 1, indexData.length)
                            array[index] = parseJson(dataJson, dataType)
                        }
                    }
                    return array
                }
                java.util.Collection::class.java.isAssignableFrom(clazz) -> {
                    // 初始化集合对象
                    val collection = if ("java.util.Arrays.ArrayList" == clazz.canonicalName) {
                        arrayListOf<Any>()
                    } else UnSafeApi.getInstance(clazz) ?: return null
                    // 反射得到集合的add方法
                    val methodAdd = try {
                        clazz.getDeclaredMethod("add", Object().javaClass)
                    } catch (e: Exception) {
                        null
                    }
                    methodAdd?.isAccessible = true
                    // 添加元素
                    for (index in 0 until data.length()) {
                        val indexData = data[index]?.toString() ?: ""
                        val typeIndex = indexData.indexOfFirst { "*" == it.toString() }
                        if (typeIndex <= 0 || typeIndex == indexData.length) {
                            if (data[index] is JSONObject) {
                                val dataType = (data[index] as JSONObject).optString("type")
                                if (dataType.isEmpty()) methodAdd?.invoke(collection, null)
                                else methodAdd?.invoke(collection, parseJson(indexData, dataType))
                            } else methodAdd?.invoke(collection, null)
                        } else {
                            val dataType = indexData.substring(0, typeIndex)
                            val dataJson = indexData.substring(typeIndex + 1, indexData.length)
                            methodAdd?.invoke(collection, parseJson(dataJson, dataType))
                        }
                    }
                    return collection
                }
                else -> null
            }
        }

        // TODO 对象类型Json解析（需要添加map构造和内部数据解析逻辑，注意key 为 "null"或split后为空串的情况）
        private fun getInstance(data: JSONObject, clazz: Class<*>): Any? {
            return when {
                java.util.Map::class.java.isAssignableFrom(clazz) -> {
                    // 初始化集合对象
                    val map = UnSafeApi.getInstance(clazz) ?: return null
                    // 反射得到集合的put方法
                    val methodPut = try {
                        clazz.getDeclaredMethod("put", Object().javaClass)
                    } catch (e: Exception) {
                        null
                    }
                    methodPut?.isAccessible = true
                    // 添加元素
                    data.keys().forEach { key ->

                    }
                    map
                }
                else -> {
                    // 初始化对象
                    val instance = UnSafeApi.getInstance(clazz) ?: return null
                    // 成员变量赋值
                    for (f in getFields(clazz)) {
                        UnSafeApi.setFieldData(data, f, instance)
                    }
                    instance
                }
            }
        }

        private fun getFields(inputClass: Class<*>): LinkedList<Field> {
            // 初始化结果
            val result = LinkedList<Field>()
            // 初始化排重map并填充
            val map = LinkedHashMap<String, Field>()
            val classes = getClasses(inputClass)
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
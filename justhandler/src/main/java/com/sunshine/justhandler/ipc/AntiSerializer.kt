package com.sunshine.justhandler.ipc

import com.sunshine.justhandler.ipc.serializer.UnSafeApi
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
        fun parseJson(json: String): Any? {
            val jsonObj = try {
                JSONObject(json)
            } catch (e: JSONException) {
                return null
            }
            val type = jsonObj.optString("type")
            val list = jsonObj.optJSONArray("list")
            val map = jsonObj.optJSONObject("map")
            val data = jsonObj.optJSONObject("data")
            var result: Any? = data?.toString()
            if (type.isNullOrEmpty()) return result
            // 获取class对象
            val clazz = Class.forName(type)
            // 获取目标对象
            try {
                if (data != null) {
                    val obj = getInstance(data, clazz)
                    if (obj != null) result = obj
                } else if (map != null) {
                    val obj = getInstance(map, clazz)
                    if (obj != null) result = obj
                } else if (list != null) {
                    val obj = getInstance(list, clazz)
                    if (obj != null) result = obj
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }

        private fun getInstance(data: JSONArray, clazz: Class<*>): Any? {
            return when {
                clazz.isArray -> {
                    return null
                }
                Collection::class.java.isAssignableFrom(clazz) -> {
                    return null
                }
                else -> null
            }
        }

        private fun getInstance(data: JSONObject, clazz: Class<*>): Any? {
            return when {
                Map::class.java.isAssignableFrom(clazz) -> {
                    null
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
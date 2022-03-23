package com.sunshine.justhandler.ipc

import android.annotation.SuppressLint
import org.json.JSONObject
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.*
import java.util.Collection
import java.util.Map
import kotlin.collections.LinkedHashMap

/**
 * created by: Sunshine at 2022/2/27
 * desc: 反序列化工具
 */
class AntiSerializer {
    companion object {
        private val unSafeClass by lazy {
            Class.forName("sun.misc.Unsafe")
        }
        private val unSafe by lazy {
            val theUnsafeField = unSafeClass.getDeclaredField("theUnsafe")
            theUnsafeField.isAccessible = true
            theUnsafeField.get(null)
        }
        private val allocateInstance by lazy {
            unSafeClass.getMethod("allocateInstance", Class::class.java)
        }
        private val objectFieldOffset by lazy {
            unSafeClass.getMethod("objectFieldOffset", Field::class.java)
        }


        fun getData(json: String): Any? {
            val jsonObj = JSONObject(json)
            parseJsonObj(jsonObj)

//            try {
//                val clazz = Class.forName("com.example.justhandler.testBean.MsgBean")
//                val field = clazz.getDeclaredField("a")
//                field.isAccessible = true
//                Log.i("haha1", field.name)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }

            return null
        }

        private fun parseJsonObj(jsonObj: JSONObject?): Any? {
            val type = jsonObj?.optString("type")
            val data = jsonObj?.optJSONObject("data")
            var result: Any? = data?.toString()
            if (data == null || type.isNullOrEmpty()) return result
            try {
                // 获取class对象
                val clazz = Class.forName(type)
                // 构造目标对象
                val obj = getInstance(data, clazz)
                // 返回生成的对象
                result = obj
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }

        @SuppressLint("DiscouragedPrivateApi")
        private fun <T> getInstance(data: JSONObject, clazz: Class<T>): T? {
            when {
                clazz.isArray -> {
                    return null
                }
                Collection::class.java.isAssignableFrom(clazz) -> {
                    return null
                }
                Map::class.java.isAssignableFrom(clazz) -> {
                    return null
                }
                else -> return try {
                    // 初始化对象
                    val instance = allocateInstance.invoke(unSafe, clazz)
                    // 成员变量赋值
                    val fields = getFields(clazz)
                    for (f in fields) {
                        val value = data.opt(f.name)
                        val fOffset = objectFieldOffset.invoke(unSafe, f)
                    }
                    return instance as T
                } catch (e: Exception) {
                    null
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
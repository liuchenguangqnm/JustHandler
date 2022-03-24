package com.sunshine.justhandler.ipc.serializer

import com.sunshine.justhandler.ipc.AntiSerializer
import org.json.JSONObject
import java.lang.Exception
import java.lang.reflect.Field

/**
 * created by: Sunshine at 2022/3/23
 * desc: 反射new对象并直接修改成员内存值得到目标对象
 */
object UnSafeApi {
    private val unSafeClass by lazy {
        Class.forName("sun.misc.Unsafe")
    }
    private val unSafe by lazy {
        val theUnsafeField = unSafeClass.getDeclaredField("theUnsafe")
        theUnsafeField.isAccessible = true
        theUnsafeField.get(null)
    }
    private val objectFieldOffset by lazy {
        unSafeClass.getMethod("objectFieldOffset", Field::class.java)
    }
    private val putObject by lazy {
        unSafeClass.getMethod(
            "putObject", Object::class.java, Long::class.java, Object::class.java
        )
    }

    // 通过反射new对象
    fun getInstance(clazz: Class<*>): Any? {
        val constructors = clazz.declaredConstructors
        if (constructors.isEmpty()) return null
        constructors[0].isAccessible = true
        val initArgTypes = constructors[0].parameterTypes
        val initArgs = arrayOfNulls<Any>(constructors[0].parameterTypes.size)
        for (index in initArgTypes.indices) {
            val argType = initArgTypes[index]
            initArgs[index] = when {
                java.lang.String::class.java.isAssignableFrom(argType) -> ""
                java.lang.Integer::class.java.isAssignableFrom(argType) -> 0
                java.lang.Long::class.java.isAssignableFrom(argType) -> 0L
                java.lang.Float::class.java.isAssignableFrom(argType) -> 0f
                java.lang.Double::class.java.isAssignableFrom(argType) -> 0.0
                java.lang.Boolean::class.java.isAssignableFrom(argType) -> false
                argType.isArray -> initArgs[index] = arrayOfNulls<Any?>(0)
                java.util.Collection::class.java.isAssignableFrom(argType) -> listOf<Any?>()
                java.util.Map::class.java.isAssignableFrom(argType) -> mapOf<Any?, Any?>()
                else -> getInstance(argType)
            }
        }
        return try {
            constructors[0].newInstance(*initArgs)
        } catch (e: Exception) {
            null
        }
    }

    // 设置对象成员变量的值
    fun setFieldData(jsonObj: JSONObject, field: Field, instance: Any?) {
        val fData = getFieldData(jsonObj, field) ?: return
        val fOffset = objectFieldOffset.invoke(unSafe, field) ?: return
        putObject.invoke(unSafe, instance, fOffset, fData)
    }

    // 解析Json成员变量的值
    private fun getFieldData(obj: JSONObject, field: Field): Any? {
        return when {
            java.lang.String::class.java.isAssignableFrom(field.type) -> {
                obj.optString(field.name)
            }
            java.lang.Integer::class.java.isAssignableFrom(field.type) -> {
                obj.optInt(field.name).toString().toInt()
            }
            java.lang.Long::class.java.isAssignableFrom(field.type) -> {
                obj.optLong(field.name).toString().toLong()
            }
            java.lang.Float::class.java.isAssignableFrom(field.type) -> {
                obj.optDouble(field.name, .0).toString().toFloat()
            }
            java.lang.Double::class.java.isAssignableFrom(field.type) -> {
                obj.optDouble(field.name, .0).toString().toDouble()
            }
            java.lang.Boolean::class.java.isAssignableFrom(field.type) -> {
                obj.optBoolean(field.name).toString().toBoolean()
            }
            else -> {
                val fData = obj.opt(field.name)
                return if (fData == null) null
                else AntiSerializer.parseJson(fData.toString())
            }
        }
    }
}
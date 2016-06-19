package com.github.yoojia.web.core

import com.github.yoojia.web.util.checkObjectType
import java.util.*
import kotlin.reflect.KClass

/**
 * @author Yoojia Chen (yoojia.chen@gmail.com)
 * @since 2.0
 */
class Config(val values: Map<String, Any>) {

    @Suppress("UNCHECKED_CAST")
    fun getConfigList(key: String): List<Config> {
        val out = ArrayList<Config>()
        getTypedList<Map<String, Any>>(key).forEach { map->
            out.add(Config(map))
        }
        return out.toList()
    }

    @Suppress("UNCHECKED_CAST")
    fun getConfig(key: String): Config {
        val map = getChecked(key, emptyMap<String, Any>(), Map::class)
        return Config(map)
    }

    fun getString(key: String): String {
        return getString(key, "")
    }

    fun getString(key: String, def: String): String {
        return getChecked(key, def, String::class)
    }

    fun getInt(key: String): Int {
        return getInt(key, 0)
    }

    fun getInt(key: String, def: Int): Int {
        return getChecked(key, def, Int::class)
    }

    fun getLong(key: String): Long {
        return getLong(key, 0)
    }

    fun getLong(key: String, def: Long): Long {
        return getChecked(key, def, Long::class)
    }

    fun getBoolean(key: String): Boolean {
        return getBoolean(key, false)
    }

    fun getBoolean(key: String, def: Boolean): Boolean {
        return getChecked(key, def, Boolean::class)
    }

    fun getFloat(key: String): Float {
        return getFloat(key, 0f)
    }

    fun getFloat(key: String, def: Float): Float {
        return getChecked(key, def, Float::class)
    }

    fun getDouble(key: String): Double {
        return getDouble(key, 0.0)
    }

    fun getDouble(key: String, def: Double): Double {
        return getChecked(key, def, Double::class)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getTypedList(key: String): List<T> {
        return getChecked(key, emptyList(), List::class)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getChecked(key: String, def: T, type: KClass<*>): T {
        values[key]?.let { value ->
            checkObjectType(value, type)
            return value as T
        }
        return def
    }

    fun containsKey(key: String): Boolean {
        return values.containsKey(key)
    }

    fun getValueType(key: String): Class<*> {
        val value = values[key]
        if(value != null) {
            return value.javaClass
        }else{
            throw IllegalArgumentException("Value of key: $key is not exists")
        }
    }

    override fun toString(): String {
        return values.toString()
    }
}
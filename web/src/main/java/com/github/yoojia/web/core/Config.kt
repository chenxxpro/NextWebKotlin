package com.github.yoojia.web.core

import java.util.*

/**
 * @author Yoojia Chen (yoojia.chen@gmail.com)
 * @since 2.0
 */
class Config(val values: Map<String, Any>) {

    @Suppress("UNCHECKED_CAST")
    fun <T> getTypedList(key: String): List<T> {
        val value = values[key]
        if(value != null) {
            val list = value as ArrayList<T>
            return list.toList()
        }else{
            return emptyList()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getConfigList(key: String): List<Config> {
        val out = ArrayList<Config>()
        val value = values[key]
        if(value != null) {
            val list = value as ArrayList<LinkedHashMap<String, Any>>
            list.forEach { out.add(Config(it)) }
        }
        return out.toList()
    }

    @Suppress("UNCHECKED_CAST")
    fun getConfig(key: String): Config {
        val value = values[key]
        if(value != null) {
            return Config(value as LinkedHashMap<String, Any>)
        }else{
            return Config(emptyMap())
        }
    }

    fun getString(key: String): String {
        return getString(key, "")
    }

    fun getString(key: String, def: String): String {
        val value = values[key]
        if(value != null) {
            return value as String
        }else{
            return def
        }
    }

    fun getInt(key: String): Int {
        return getInt(key, 0)
    }

    fun getInt(key: String, def: Int): Int {
        val value = values[key]
        if(value != null) {
            return value as Int
        }else{
            return def
        }
    }

    fun getBoolean(key: String): Boolean {
        return getBoolean(key, false)
    }

    fun getBoolean(key: String, def: Boolean): Boolean {
        val value = values[key]
        if(value != null) {
            return value as Boolean
        }else{
            return def
        }
    }

    fun getFloat(key: String): Float {
        return getFloat(key, 0f)
    }

    fun getFloat(key: String, def: Float): Float {
        val value = values[key]
        if(value != null) {
            return value as Float
        }else{
            return def
        }
    }

    override fun toString(): String {
        return values.toString()
    }
}
package com.github.yoojia.web.kernel

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
        val value = values[key]
        if(value != null) {
            return value as String
        }else{
            return ""
        }
    }

    fun getInt(key: String): Int {
        val value = values[key]
        if(value != null) {
            return value as Int
        }else{
            return 0
        }
    }

    fun getBoolean(key: String): Boolean {
        val value = values[key]
        if(value != null) {
            return "true".equals(value)
        }else{
            return false
        }
    }

    fun getFloat(key: String): Float {
        val value = values[key]
        if(value != null) {
            return value as Float
        }else{
            return 0f
        }
    }

    override fun toString(): String {
        return values.toString()
    }
}
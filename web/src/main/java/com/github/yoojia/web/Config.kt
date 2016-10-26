package com.github.yoojia.web

import com.github.yoojia.lang.DataMap
import java.util.*

/**
 * @author Yoojia Chen (yoojia.chen@gmail.com)
 * @since 2.0
 */
class Config(realMap: MutableMap<String, Any>?) : DataMap(realMap) {

    companion object {
        @JvmStatic fun empty(): Config = Config(EMPTY)
    }

    @Suppress("UNCHECKED_CAST")
    fun getConfigList(key: String): List<Config> {
        val out = ArrayList<Config>()
        getTypedList<MutableMap<String, Any>>(key).forEach { map->
            out.add(Config(map))
        }
        return out.toList()
    }

    @Suppress("UNCHECKED_CAST")
    fun getConfig(key: String): Config {
        val value = get(key)
        if(value == null) {
            return Config(EMPTY)
        }else{
            val map = value as MutableMap<String, Any>
            return Config(map)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getTypedList(key: String): List<T> {
        val value = get(key)
        if(value == null) {
            return emptyList()
        }else{
            return value as List<T>
        }
    }

    fun getValueType(key: String): Class<*> {
        val value = get(key)
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
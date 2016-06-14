package com.github.yoojia.web.util

import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.5.1
 */
open class AnyMap(val realMap: MutableMap<String, Any>) : MutableMap<String, Any> {

    override val entries: MutableSet<MutableMap.MutableEntry<String, Any>>
        get() = realMap.entries

    override val keys: MutableSet<String>
        get() = realMap.keys

    override val values: MutableCollection<Any>
        get() = realMap.values

    override val size: Int
        get() = realMap.size

    // Default is a HashMap<String, Any>
    constructor(): this(HashMap<String, Any>())

    override fun clear() {
        realMap.clear()
    }

    override fun put(key: String, value: Any): Any? {
        return realMap.put(key, value)
    }

    override fun putAll(from: Map<out String, Any>) {
        return realMap.putAll(from)
    }

    override fun remove(key: String): Any? {
        return realMap.remove(key)
    }

    override fun containsKey(key: String): Boolean {
        return realMap.containsKey(key)
    }

    override fun containsValue(value: Any): Boolean {
        return realMap.containsValue(value)        
    }

    override fun get(key: String): Any? {
        return realMap[key]
    }

    override fun isEmpty(): Boolean {
        return realMap.isEmpty()
    }

    /// Extensions
    /// Q: Why not Kotlin extension feature?
    /// A: For Java usage

    fun get(key: String, defValue: String = ""): String {
        return getCastType(key, defValue)
    }

    fun get(key: String, defValue: Int = 0): Int {
        return getCastType(key, defValue)
    }

    fun get(key: String, defValue: Long = 0): Long {
        return getCastType(key, defValue)
    }

    fun get(key: String, defValue: Float= 0f): Float {
        return getCastType(key, defValue)
    }

    fun get(key: String, defValue: Double = 0.0): Double {
        return getCastType(key, defValue)
    }

    fun get(key: String, defValue: Boolean = false): Boolean {
        return getCastType(key, defValue)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getCastType(key: String, defValueValue: T): T {
        val value = get(key)
        if(value == null) {
            return defValueValue
        }else{
            return value as T
        }
    }

}
package com.github.yoojia.web.supports

import com.github.yoojia.web.supports.newClassInstance
import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class ObjectProvider(guessSize: Int) {

    private val mCached = object: LinkedHashMap<Class<*>, Any>(guessSize, 0.75f, true){

        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Class<*>, Any>?): Boolean {
            return this.size > guessSize
        }

    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(type: Class<*>): T {
        synchronized(mCached) {
            var obj = mCached[type]
            if(obj == null) {
                obj = newClassInstance(type)
                mCached.put(type, obj!!)
            }
            return obj as T
        }
    }

}
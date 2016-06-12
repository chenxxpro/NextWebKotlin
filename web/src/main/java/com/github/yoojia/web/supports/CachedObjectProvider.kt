package com.github.yoojia.web.supports

import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class CachedObjectProvider(guessSize: Int) {

    private val mCached = object: LinkedHashMap<Class<*>, Any>(guessSize, 0.75f, true){

        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Class<*>, Any>?): Boolean {
            if(eldest?.value is CachedObjectListener) {
                (eldest!!.value as CachedObjectListener).onDestroy()
            }
            return this.size > guessSize
        }

    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(type: Class<*>): T {
        synchronized(mCached) {
            var obj = mCached[type]
            if(obj == null) {
                obj = newClassInstance(type)
                if(obj is CachedObjectListener) {
                    obj.onCreated()
                }
                mCached.put(type, obj!!)
            }
            return obj as T
        }
    }

}
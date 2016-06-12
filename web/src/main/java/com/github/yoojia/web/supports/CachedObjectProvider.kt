package com.github.yoojia.web.supports

import com.github.yoojia.web.util.newClassInstance
import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class CachedObjectProvider(guessSize: Int) {

    private val mCached = object: LinkedHashMap<Class<*>, Any>(guessSize, 0.75f, true){

        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Class<*>, Any>?): Boolean {
            val toRemove = this.size > guessSize
            if(toRemove && eldest?.value is CachedObjectListener) {
                (eldest!!.value as CachedObjectListener).onDestroy()
            }
            return toRemove
        }

    }

    fun get(type: Class<*>): Any {
        synchronized(mCached) {
            var obj = mCached[type]
            if(obj == null) {
                obj = newClassInstance(type)
                if(obj is CachedObjectListener) {
                    obj.onCreated()
                }
                mCached.put(type, obj!!)
            }
            return obj
        }
    }

}
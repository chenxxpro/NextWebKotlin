package com.github.yoojia.web.supports

import com.github.yoojia.web.util.newClassInstance
import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class ModuleCachedProvider(guessSize: Int) {

    private val mCached = object: LinkedHashMap<Class<*>, Any>(guessSize, 0.75f, true){

        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Class<*>, Any>?): Boolean {
            val toRemove = this.size > guessSize
            if(toRemove && eldest?.value is ModuleCachedListener) {
                (eldest!!.value as ModuleCachedListener).onRemoved()
            }
            return toRemove
        }

    }

    fun get(type: Class<*>): Any {
        var isCachedAction = false
        var tmpObject: Any? = null
        synchronized(mCached) {
            var cached = mCached[type]
            if(cached == null) {
                cached = newClassInstance(type)
                mCached.put(type, cached!!)
                isCachedAction = true
            }
            tmpObject = cached
        }
        val module = tmpObject!!
        try{
            return module
        }finally{
            if(isCachedAction && module is ModuleCachedListener) {
                module.onCached()
            }
        }
    }

}
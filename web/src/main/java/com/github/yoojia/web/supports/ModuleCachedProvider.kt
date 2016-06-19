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
            val remove = this.size > guessSize
            if(remove && eldest?.value is ModuleCachedListener) {
                (eldest!!.value as ModuleCachedListener).onRemoved()
            }
            return remove
        }

    }

    fun get(type: Class<*>): Any {
        var isCached = false
        var obj: Any? = null
        synchronized(mCached) {
            var cached = mCached[type]
            if(cached == null) {
                cached = newClassInstance(type)
                mCached.put(type, cached!!)
                isCached = true
            }
            obj = cached
        }
        val module = obj!!
        try{
            return module
        }finally{
            if(isCached && module is ModuleCachedListener) {
                module.onCached()
            }
        }
    }

}
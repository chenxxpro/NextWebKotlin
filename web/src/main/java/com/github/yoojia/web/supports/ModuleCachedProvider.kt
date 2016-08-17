package com.github.yoojia.web.supports

import com.github.yoojia.web.util.newClassInstance
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class ModuleCachedProvider(guessSize: Int) {

    private val mCached = object: LinkedHashMap<Class<*>, Any>(guessSize, 0.75f, true){

        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Class<*>, Any>?): Boolean {
            val isRemoved = this.size > guessSize
            try{
                return isRemoved
            }finally {
                val module = eldest!!.value
                if(isRemoved && module is ModuleCachedListener) {
                    module.onRemoved()
                }
            }
        }

    }

    private val resourceLock = ReentrantLock()

    fun get(type: Class<*>): Any {
        var isCached = false
        val lock = resourceLock
        val module: Any
        lock.lock()
        try{
            var cached = mCached[type]
            if(cached == null) {
                cached = newClassInstance(type)
                mCached.put(type, cached!!)
                isCached = true
            }
            module = cached
        }finally {
            lock.unlock()
        }
        try{
            return module
        }finally{
            if(isCached && module is ModuleCachedListener) {
                module.onCached()
            }
        }
    }

}
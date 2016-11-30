package com.github.yoojia.web.supports

import com.github.yoojia.web.lang.newClassInstance
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class ModuleCachedProvider(guessSize: Int) {

    private val resourceLock = ReentrantLock()
    private val cached = object: LinkedHashMap<Class<*>, Any>(guessSize, 0.75f, true){

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

    fun getOrNew(type: Class<*>): Any {
        var isCached = false
        val module: Any
        val lock = resourceLock
        lock.lock()
        try{
            var cached = cached[type]
            if(cached == null) {
                cached = newClassInstance(type)
                this.cached.put(type, cached!!)
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
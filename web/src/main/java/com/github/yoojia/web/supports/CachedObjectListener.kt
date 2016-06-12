package com.github.yoojia.web.supports

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.3
 */
interface CachedObjectListener {

    fun onCreated()

    fun onDestroy()
}
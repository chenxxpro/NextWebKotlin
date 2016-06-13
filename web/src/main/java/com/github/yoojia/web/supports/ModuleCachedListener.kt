package com.github.yoojia.web.supports

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.5
 */
interface ModuleCachedListener : ModuleListener{

    /**
     * 模块对象被缓存管理器创建后回调
     */
    fun onCached()

    /**
     * 模块对象被缓存管理器移除时回调
     */
    fun onRemoved()
}
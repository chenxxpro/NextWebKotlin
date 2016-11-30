package com.github.yoojia.web

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.2
 */
interface Lifecycle {

    /**
     * 当引擎初始化完成后并启动时调用此方法。
     */
    fun onCreated(context: Context, config: Config)

    /**
     * 当引擎关闭时调用此方法。
     */
    fun onDestroy()
}
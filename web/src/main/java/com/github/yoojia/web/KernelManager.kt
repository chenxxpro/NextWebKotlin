package com.github.yoojia.web

import java.util.*

/**
 * @author Yoojia Chen (yoojia.chen@gmail.com)
 * @since 1.0
 */
internal class KernelManager {

    private val modules = ArrayList<KernelItem<Module>>()
    private val plugins = ArrayList<KernelItem<Plugin>>()

    fun register(plugin: Plugin, priority: Int, config: Config) {
        plugins.add(KernelItem(plugin, priority, config))
    }

    fun register(module: Module, priority: Int, config: Config) {
        modules.add(KernelItem(module, priority, config))
    }

    internal fun modules(action: (Module) -> Unit) {
        modules.forEach { action.invoke(it.payload) }
    }

    internal fun created(ctx: Context) {
        modules.sortedBy { it.priority }.forEach { it.payload.onCreated(ctx, it.conf) }
        plugins.sortedBy { it.priority }.forEach { it.payload.onCreated(ctx, it.conf) }
    }

    internal fun destroy() {
        // Call onDestroy
        modules.forEach { it.payload.onDestroy() }
        modules.clear()
        plugins.forEach { it.payload.onDestroy() }
        plugins.clear()
    }

    internal fun moduleCount(): Int {
        return modules.size
    }

    internal fun pluginCount(): Int {
        return plugins.size
    }

    /**
     * 配置参数数据包装.不使用Triple的是为使得参数意义更新清晰.
     */
    private data class KernelItem<out T: Lifecycle>(val payload: T, val priority: Int, val conf: Config)

}
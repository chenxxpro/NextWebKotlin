package com.github.yoojia.web

import java.util.*

/**
 * @author Yoojia Chen (yoojia.chen@gmail.com)
 * @since 1.0
 */
internal class KernelManager {

    private val modules = ArrayList<Invoker<Module>>()
    private val plugins = ArrayList<Invoker<Plugin>>()

    fun register(plugin: Plugin, priority: Int, config: Config) {
        plugins.add(Invoker(plugin, priority, config))
    }

    fun register(module: Module, priority: Int, config: Config) {
        modules.add(Invoker(module, priority, config))
    }

    internal fun modules(action: (Module) -> Unit) {
        modules.forEach { action.invoke(it.invoker) }
    }

    internal fun created(ctx: Context) {
        modules.sortedBy { it.priority }.forEach { it.invoker.onCreated(ctx, it.config) }
        plugins.sortedBy { it.priority }.forEach { it.invoker.onCreated(ctx, it.config) }
    }

    internal fun destroy() {
        // Call onDestroy
        modules.forEach { it.invoker.onDestroy() }
        modules.clear()
        plugins.forEach { it.invoker.onDestroy() }
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
    private data class Invoker<out T: Lifecycle>(val invoker: T, val priority: Int, val config: Config)

}
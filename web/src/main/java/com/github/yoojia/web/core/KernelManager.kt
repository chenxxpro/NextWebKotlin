package com.github.yoojia.web.core

import java.util.*

/**
 * @author Yoojia Chen (yoojia.chen@gmail.com)
 * @since 1.0
 */
class KernelManager {

    private val modules = ArrayList<Triple<Module, Int, Config>>()
    private val plugins = ArrayList<Triple<Plugin, Int, Config>>()

    fun register(plugin: Plugin, priority: Int, config: Config) {
        plugins.add(Triple(plugin, priority, config))
    }

    fun register(module: Module, priority: Int, config: Config) {
        modules.add(Triple(module, priority, config))
    }

    internal fun withModules(action: (Module) -> Unit) {
        modules.forEach { action.invoke(it.first) }
    }

    internal fun onCreated(ctx: Context) {
        modules.sortedBy { it.second/*priority*/ }.forEach { it.first/*model*/.onCreated(ctx, it.third/*config*/) }
        plugins.sortedBy { it.second/*priority*/ }.forEach { it.first/*plugin*/.onCreated(ctx, it.third/*config*/) }
    }

    internal fun onDestroy() {
        // Call onDestroy
        modules.forEach { it.first.onDestroy() }
        modules.clear()
        plugins.forEach { it.first.onDestroy() }
        plugins.clear()
    }

    internal fun moduleCount(): Int {
        return modules.size
    }

    internal fun pluginCount(): Int {
        return plugins.size
    }
}
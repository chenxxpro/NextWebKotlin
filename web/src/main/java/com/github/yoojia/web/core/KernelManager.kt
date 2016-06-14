package com.github.yoojia.web.core

import java.util.*

/**
 * @author Yoojia Chen (yoojia.chen@gmail.com)
 * @since 1.0
 */
internal class KernelManager {

    private val modules = ArrayList<Triple<Module, Int, Config>>()
    private val plugins = ArrayList<Triple<Plugin, Int, Config>>()

    fun allModules(action: (Module) -> Unit) {
        modules.forEach { action.invoke(it.first) }
    }

    fun register(plugin: Plugin, priority: Int, config: Config) {
        plugins.add(Triple(plugin, priority, config))
    }

    fun register(module: Module, priority: Int, config: Config) {
        modules.add(Triple(module, priority, config))
    }

    fun onCreated(ctx: Context) {
        modules.sortedBy { it.second/*priority*/ }.forEach { it.first/*model*/.onCreated(ctx, it.third/*config*/) }
        plugins.sortedBy { it.second/*priority*/ }.forEach { it.first/*plugin*/.onCreated(ctx, it.third/*config*/) }
    }

    fun onDestroy() {
        // Call onDestroy
        modules.forEach { it.first.onDestroy() }
        modules.clear()
        plugins.forEach { it.first.onDestroy() }
        plugins.clear()
    }

    fun moduleCount(): Int {
        return modules.size
    }

    fun pluginCount(): Int {
        return plugins.size
    }
}
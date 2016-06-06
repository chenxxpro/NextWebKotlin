package com.github.yoojia.web.kernel

import java.util.*

/**
 * @author Yoojia Chen (yoojia.chen@gmail.com)
 * @since 1.0
 */
internal class KernelManager {

    private val mModules = ArrayList<Triple<Module, Int, Config>>()
    private val mPlugins = ArrayList<Triple<Plugin, Int, Config>>()

    fun modulesForEach(step: (Module) -> Unit) {
        mModules.forEach { step.invoke(it.first) }
    }

    fun register(plugin: Plugin, priority: Int, config: Config) {
        mPlugins.add(Triple(plugin, priority, config))
    }

    fun register(module: Module, priority: Int, config: Config) {
        mModules.add(Triple(module, priority, config))
    }

    fun onCreated(ctx: Context) {
        // Sort by priority
        mModules.sortBy { it.second/*priority*/ }
        // Then call onCreated
        mModules.forEach { it.first.onCreated(ctx, it.third) }
    }

    fun onDestroy() {
        // Call onDestroy
        mModules.forEach {  it.first.onDestroy() }
        mModules.clear()
        mPlugins.clear()
    }

    fun moduleCount(): Int {
        return mModules.size
    }

    fun pluginCount(): Int {
        return mPlugins.size
    }
}
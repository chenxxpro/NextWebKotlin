package com.github.yoojia.web.core

import java.util.*

/**
 * @author Yoojia Chen (yoojia.chen@gmail.com)
 * @since 1.0
 */
internal class KernelManager {

    private val mModules = ArrayList<Triple<Module, Int, Config>>()
    private val mPlugins = ArrayList<Triple<Plugin, Int, Config>>()

    fun allModules(action: (Module) -> Unit) {
        mModules.forEach { action.invoke(it.first) }
    }

    fun register(plugin: Plugin, priority: Int, config: Config) {
        mPlugins.add(Triple(plugin, priority, config))
    }

    fun register(module: Module, priority: Int, config: Config) {
        mModules.add(Triple(module, priority, config))
    }

    fun onCreated(ctx: Context) {
        mModules.sortedBy { it.second/*priority*/ }.forEach { it.first.onCreated(ctx, it.third) }
        mPlugins.sortedBy { it.second/*priority*/ }.forEach { it.first.onCreated(ctx, it.third) }
    }

    fun onDestroy() {
        // Call onDestroy
        mModules.forEach {  it.first.onDestroy() }
        mModules.clear()
        mPlugins.forEach {  it.first.onDestroy() }
        mPlugins.clear()
    }

    fun moduleCount(): Int {
        return mModules.size
    }

    fun pluginCount(): Int {
        return mPlugins.size
    }
}
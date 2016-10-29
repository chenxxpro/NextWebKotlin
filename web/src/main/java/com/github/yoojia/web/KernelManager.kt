package com.github.yoojia.web

import java.util.*

/**
 * @author Yoojia Chen (yoojia.chen@gmail.com)
 * @since 1.0
 */
internal class KernelManager {

    private val https = ArrayList<Invoker<Module>>()
    private val plugins = ArrayList<Invoker<Plugin>>()
    private val before = ArrayList<Invoker<Module>>()
    private val after = ArrayList<Invoker<Module>>()

    val sortedHttps: List<Invoker<Module>> by lazy { https.sortedBy { it.priority } }
    val sortedPlugins: List<Invoker<Plugin>> by lazy { plugins.sortedBy { it.priority } }
    val sortedBefore: List<Invoker<Module>> by lazy { before.sortedBy { it.priority } }
    val sortedAfter: List<Invoker<Module>> by lazy { after.sortedBy { it.priority } }

    fun registerPlugin(plugin: Plugin, priority: Int, config: Config) {
        plugins.add(Invoker(plugin, priority, config))
    }

    fun registerHttp(module: Module, priority: Int, config: Config) {
        https.add(Invoker(module, priority, config))
    }

    fun registerBefore(module: Module, priority: Int, config: Config) {
        before.add(Invoker(module, priority, config))
    }

    fun registerAfter(module: Module, priority: Int, config: Config) {
        after.add(Invoker(module, priority, config))
    }

    fun https(): List<Module> = sortedHttps.map { it.invoker }
    fun befores(): List<Module> = sortedBefore.map { it.invoker }
    fun afters(): List<Module> = sortedAfter.map { it.invoker }

    fun created(ctx: Context) {
        sortedPlugins.forEach { it.invoker.onCreated(ctx, it.config) }
        sortedBefore.forEach { it.invoker.onCreated(ctx, it.config) }
        sortedHttps.forEach { it.invoker.onCreated(ctx, it.config) }
        sortedAfter.forEach { it.invoker.onCreated(ctx, it.config) }
    }

    fun destroy() {
        sortedBefore.forEach { it.invoker.onDestroy() }
        before.clear()
        sortedHttps.forEach { it.invoker.onDestroy() }
        https.clear()
        sortedAfter.forEach { it.invoker.onDestroy() }
        after.clear()
        sortedPlugins.forEach { it.invoker.onDestroy() }
        plugins.clear()
    }

    fun moduleCount(): Int {
        return https.size
    }

    fun pluginCount(): Int {
        return plugins.size
    }

    /**
     * 配置参数数据包装.不使用Triple的是为使得参数意义更新清晰.
     */
    data class Invoker<out T: Lifecycle>(val invoker: T, val priority: Int, val config: Config)

}
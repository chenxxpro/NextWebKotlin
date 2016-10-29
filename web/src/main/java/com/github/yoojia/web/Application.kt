package com.github.yoojia.web

import com.github.yoojia.web.http.HttpHandler
import com.github.yoojia.web.interceptor.AfterHandler
import com.github.yoojia.web.interceptor.BeforeHandler
import com.github.yoojia.web.supports.InternalPriority
import com.github.yoojia.web.util.*
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import javax.servlet.ServletContext
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
object Application {

    const val PRODUCT_NAME = "NextWebKotlin"
    const val VERSION = "$PRODUCT_NAME/2.24-ALPHA (Kotlin 1.0.4; Java 7/8;)"

    private val CONFIG_FILE = "WEB-INF${File.separator}next.yml"

    private val Logger = LoggerFactory.getLogger(Application::class.java)

    private val beforeRouter = Router()
    private val afterRouter = Router()
    private val httpRouter = Router()

    private val appContext = AtomicReference<Context>()

    private val kernels = KernelManager()

    fun setup(servletContext: ServletContext) {
        startup(servletContext, YamlConfigLoader(), RuntimeClassProvider())
    }

    fun startup(servletContext: ServletContext, configProvider: ConfigProvider, classProvider: ClassProvider) {
        Logger.warn("$PRODUCT_NAME starting, version: $VERSION")
        // init
        val start = now()
        val path = servletContext.getRealPath("/")
        val config = configProvider.getConfig(Paths.get(path, CONFIG_FILE))
        val ctx = Context(path, config, servletContext)
        appContext.set(ctx)
        init(ctx, classProvider.getClasses(ctx).toMutableList())
        // register to routers
        kernels.befores().forEach { beforeRouter.register(it) }
        kernels.afters().forEach { afterRouter.register(it) }
        kernels.https().forEach { httpRouter.register(it) }
        // startup
        kernels.sortedPlugins.forEach { it.invoker.onCreated(ctx, it.config) }
        kernels.sortedBefore.forEach { it.invoker.onCreated(ctx, it.config) }
        kernels.sortedHttps.forEach { it.invoker.onCreated(ctx, it.config) }
        kernels.sortedAfter.forEach { it.invoker.onCreated(ctx, it.config) }
        // Logging
        if (Logger.isDebugEnabled) {
            Logger.debug("Config-From : ${config.getStringValue(YamlConfigLoader.KEY_CONFIG_PATH)}")
            Logger.debug("Config-State: ${config.getStringValue(YamlConfigLoader.KEY_CONFIG_STATE)}")
            Logger.debug("Config-Time : ${escape(start)}ms")
            Logger.debug("App-Path: ${ctx.webPath}")
            Logger.debug("App-Context  : ${ctx.contextPath}")
            Logger.debug("Modules-Loaded: ${kernels.moduleCount()}")
            Logger.debug("Plugins-Loaded: ${kernels.pluginCount()}")
        }
        if (Logger.isWarnEnabled) {
            Logger.warn("$PRODUCT_NAME startup successful, boot-time: ${escape(start)}ms")
        }
    }

    fun service(req: ServletRequest, res: ServletResponse) {
        val context = appContext.get()
        // 接收到每个客户端请求时，独立创建 Request 和 Response 临时对象，生命周期只限于请求过程。
        val request = Request(context, req as HttpServletRequest)
        val response = Response(context, res as HttpServletResponse)
        // default configs:
        response.status(StatusCode.NOT_FOUND) // Default: 404
        response.param("app-context", request.contextPath)
        response.param("request-path", request.path)
        try{
            // 三大处理过程中，前、后拦截器必定会被执行，
            // 其中前拦截器的中断状态影响着HTTP处理模块是否被执行。
            val chain = RequestChain()
            beforeRouter.route(request, response, chain)
            if (! chain.isInterrupted) {
                httpRouter.route(request, response, chain)
            }
            afterRouter.route(request, response, chain)
        }catch(errors: Throwable) {
            if (Logger.isDebugEnabled) {
                Logger.debug("Error when processing request", errors)
            }
            try{
                response.error(errors)
            }catch(still: Throwable) {
                if (Logger.isDebugEnabled) {
                    Logger.error("Error when send ERROR to client", still)
                }
            }
        }
    }

    fun shutdown() {
        httpRouter.shutdown()
        kernels.destroy()
    }

    private fun init(context: Context, classes: MutableList<Class<*>>) {
        val rootConfig = context.rootConfig
        val prepareModules = fun(tag: String, module: Module, action: (Module)->Unit){
            val start = now()
            val scrapClasses = module.prepare(classes)
            if (scrapClasses.isNotEmpty()) {
                classes.removeAll(scrapClasses)
            }
            Logger.debug("$tag-Prepare: ${escape(start)}ms")
            action.invoke(module)
        }
        // plugins
        val pluginEntries = ArrayList<ConfigEntry>()
        pluginEntries.addAll(findPlugins(rootConfig))
        pluginEntries.forEach { entry->
            val plugin = tryPluginObject(entry)
            kernels.registerPlugin(plugin, entry.priority, entry.args)
        }
        // before interceptors
        val beforeEntries = ArrayList<ConfigEntry>()
        beforeEntries.addAll(findExtensionBefore(rootConfig))
        beforeEntries.add(ConfigEntry(BeforeHandler::class.java.name, BeforeHandler.DEFAULT_PRIORITY, Config.empty()))
        beforeEntries.forEach { entry->
            prepareModules("BeforeInterceptors", tryModuleObject(entry, classes)) { module->
                kernels.registerBefore(module, entry.priority, entry.args)
            }
        }
        // http modules
        val httpEntries = ArrayList<ConfigEntry>()
        httpEntries.add(ConfigEntry(HttpHandler::class.java.name, HttpHandler.DEFAULT_PRIORITY, Config.empty()))
        httpEntries.addAll(findModules(rootConfig))
        httpEntries.forEach { entry->
            prepareModules("HttpModules", tryModuleObject(entry, classes)) { module->
                kernels.registerHttp(module, entry.priority, entry.args)
            }
        }
        // after interceptors
        val afterEntries = ArrayList<ConfigEntry>()
        afterEntries.add(ConfigEntry(AfterHandler::class.java.name, BeforeHandler.DEFAULT_PRIORITY, Config.empty()))
        afterEntries.addAll(findExtensionAfter(rootConfig))
        afterEntries.forEach { entry->
            prepareModules("AftersInterceptors", tryModuleObject(entry, classes)) { module->
                kernels.registerAfter(module, entry.priority, entry.args)
            }
        }
    }

    private fun tryModuleObject(config: ConfigEntry, classes: MutableList<Class<*>>): Module {
        return when(config.className) {
            HttpHandler::class.java.name -> HttpHandler(classes)
            AfterHandler::class.java.name -> AfterHandler(classes)
            BeforeHandler::class.java.name -> BeforeHandler(classes)
            else -> {
                newClassInstance<Module>(getCoreClassLoader().loadClass(config.className))
            }
        }
    }

    private fun tryPluginObject(config: ConfigEntry): Plugin {
        return newClassInstance<Plugin>(getCoreClassLoader().loadClass(config.className))
    }

    private fun findPlugins(rootConfig: Config): List<ConfigEntry> {
        return findSection(rootConfig, "plugins")
    }

    private fun findExtensionBefore(rootConfig: Config): List<ConfigEntry>{
        val output = findSection(rootConfig, "before-interceptors")
        tryLoadClass("com.github.yoojia.web.HttpBeforeLogger")?.let { clazz->
            output.add(ConfigEntry(clazz.name, InternalPriority.LOGGING_BEFORE, rootConfig.getConfig("before-logger")))
        }
        tryLoadClass("com.github.yoojia.web.Assets")?.let { clazz->
            output.add(ConfigEntry(clazz.name, InternalPriority.ASSETS, rootConfig.getConfig("assets")))
        }
        return output
    }

    private fun findExtensionAfter(rootConfig: Config): List<ConfigEntry>{
        val output = findSection(rootConfig, "after-interceptors")
        tryLoadClass("com.github.yoojia.web.HttpAfterLogger")?.let { clazz->
            output.add(ConfigEntry(clazz.name, InternalPriority.LOGGING_BEFORE, rootConfig.getConfig("after-logger")))
        }
        tryLoadClass("com.github.yoojia.web.Velocity")?.let { clazz->
            output.add(ConfigEntry(clazz.name, InternalPriority.TEMPLATES, rootConfig.getConfig("template")))
        }
        return output
    }

    private fun findModules(rootConfig: Config): List<ConfigEntry>{
        return findSection(rootConfig, "modules")
    }

    private fun findSection(rootConfig: Config, section: String): MutableList<ConfigEntry> {
        return rootConfig.getConfigList(section).map { parseConfigEntry(it) }.toMutableList()
    }

    /**
     * 解析配置条目
     */
    private fun parseConfigEntry(config: Config): ConfigEntry {
        return ConfigEntry(config.getStringValue("class"),
                config.getIntValue("priority"),
                config.getConfig("args"))
    }

    /**
     * 配置参数数据包装.不使用Triple的是为使得参数意义更新清晰.
     */
    private data class ConfigEntry(val className: String, val priority: Int, val args: Config)

}

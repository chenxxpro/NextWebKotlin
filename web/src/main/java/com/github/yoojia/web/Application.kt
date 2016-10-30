package com.github.yoojia.web

import com.github.yoojia.web.http.HttpHandler
import com.github.yoojia.web.interceptor.AfterInterceptorHandler
import com.github.yoojia.web.interceptor.BeforeInterceptorHandler
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
    const val VERSION = "$PRODUCT_NAME/3.0.0-ALPHA (Kotlin 1.0.4; Java 7/8;)"

    private val CONFIG_FILE = "WEB-INF${File.separator}next.yml"

    private val Logger = LoggerFactory.getLogger(Application::class.java)

    private val preRouter = Router()
    private val postRouter = Router()
    private val moduleRouter = Router()

    private val globalContext = AtomicReference<Context>()

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
        globalContext.set(ctx)
        init(ctx, classProvider.getClasses(ctx).toMutableList())
        // register to routers
        kernels.befores().forEach { preRouter.register(it) }
        kernels.afters().forEach { postRouter.register(it) }
        kernels.modules().forEach { moduleRouter.register(it) }
        // startup
        kernels.created(ctx)
        // Logging
        if (Logger.isDebugEnabled) {
            Logger.debug("Config-From: ${config.getStringValue(YamlConfigLoader.KEY_CONFIG_PATH)}")
            Logger.debug("Config-State: ${config.getStringValue(YamlConfigLoader.KEY_CONFIG_STATE)}")
            Logger.debug("Config-Time: ${escape(start)}ms")
            Logger.debug("App-Path: ${ctx.webPath}")
            Logger.debug("App-Context: ${ctx.contextPath}")
            Logger.debug("BeforeInterceptors-Loaded: ${kernels.sortedBefore.size}")
            Logger.debug("Modules-Loaded: ${kernels.sortedModules.size}")
            Logger.debug("AfterInterceptors-Loaded: ${kernels.sortedAfter.size}")
            Logger.debug("Plugins-Loaded: ${kernels.sortedPlugins.size}")
        }
        Logger.warn("$PRODUCT_NAME startup successful, boot-time: ${escape(start)}ms")
    }

    fun service(req: ServletRequest, res: ServletResponse) {
        val context = globalContext.get()
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
            preRouter.route(request, response, chain)
            if (! chain.isInterrupted) {
                moduleRouter.route(request, response, chain)
            }
            postRouter.route(request, response, chain)
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
        preRouter.destroy()
        moduleRouter.destroy()
        postRouter.destroy()
        kernels.destroy()
    }

    private fun init(context: Context, classes: MutableList<Class<*>>) {
        val rootConfig = context.rootConfig
        // plugins
        val pluginStart = now()
        val pluginEntries = ArrayList<ConfigEntry>()
        pluginEntries.addAll(findPlugins(rootConfig))
        pluginEntries.forEach { entry->
            val plugin = tryPluginObject(entry)
            if (Logger.isDebugEnabled) Logger.debug("Preparing: ${entry.className}")
            kernels.registerPlugin(plugin, entry.priority, entry.args)
        }
        Logger.debug("Plugins-Prepare: ${escape(pluginStart)}ms")

        val prepare = fun(module: Module, action: (Module)->Unit){
            if (Logger.isDebugEnabled) Logger.debug("Preparing: ${module.javaClass.name}")
            val scrapClasses = module.prepare(classes)
            if (scrapClasses.isNotEmpty()) {
                classes.removeAll(scrapClasses)
            }
            action.invoke(module)
        }

        // before interceptors
        val beforeStart = now()
        val before = ArrayList<ConfigEntry>()
        before.add(ConfigEntry(BeforeInterceptorHandler::class.java.name, BeforeInterceptorHandler.DEFAULT_PRIORITY, Config.empty()))
        before.addAll(findExtensionBefore(rootConfig))
        before.forEach { entry->
            prepare(tryModuleObject(entry, classes)) { interceptor->
                kernels.registerBefore(interceptor, entry.priority, entry.args)
            }
        }
        Logger.debug("BeforeInterceptor-Prepare: ${escape(beforeStart)}ms")

        // http modules
        val moduleStart = now()
        val modules = ArrayList<ConfigEntry>()
        modules.add(ConfigEntry(HttpHandler::class.java.name, HttpHandler.DEFAULT_PRIORITY, Config.empty()))
        modules.addAll(findModules(rootConfig))
        modules.forEach { entry->
            prepare(tryModuleObject(entry, classes)) { module->
                kernels.registerModules(module, entry.priority, entry.args)
            }
        }
        Logger.debug("Modules-Prepare: ${escape(moduleStart)}ms")

        // after interceptors
        val afterStart = now()
        val after = ArrayList<ConfigEntry>()
        after.add(ConfigEntry(AfterInterceptorHandler::class.java.name, BeforeInterceptorHandler.DEFAULT_PRIORITY, Config.empty()))
        after.addAll(findExtensionAfter(rootConfig))
        after.forEach { entry->
            prepare(tryModuleObject(entry, classes)) { interceptor->
                kernels.registerAfter(interceptor, entry.priority, entry.args)
            }
        }
        Logger.debug("AfterInterceptor-Prepare: ${escape(afterStart)}ms")
    }

    private fun tryModuleObject(config: ConfigEntry, classes: MutableList<Class<*>>): Module {
        return when(config.className) {
            HttpHandler::class.java.name -> HttpHandler(classes)
            AfterInterceptorHandler::class.java.name -> AfterInterceptorHandler(classes)
            BeforeInterceptorHandler::class.java.name -> BeforeInterceptorHandler(classes)
            else -> {
                newClassInstance<Module>(getCoreClassLoader().loadClass(config.className))
            }
        }
    }

    private fun tryPluginObject(config: ConfigEntry): Plugin {
        return newClassInstance<Plugin>(getCoreClassLoader().loadClass(config.className))
    }

    private fun findPlugins(rootConfig: Config): List<ConfigEntry> {
        return findConfig(rootConfig, "plugins")
    }

    private fun findExtensionBefore(rootConfig: Config): List<ConfigEntry>{
        val output = findConfig(rootConfig, "before-interceptors")
        tryLoadClass("com.github.yoojia.web.HttpBeforeLogger")?.let { clazz->
            output.add(ConfigEntry(clazz.name, InternalPriority.LOGGING_BEFORE, rootConfig.getConfig("before-logger")))
        }
        tryLoadClass("com.github.yoojia.web.Assets")?.let { clazz->
            output.add(ConfigEntry(clazz.name, InternalPriority.ASSETS, rootConfig.getConfig("assets")))
        }
        return output
    }

    private fun findExtensionAfter(rootConfig: Config): List<ConfigEntry>{
        val output = findConfig(rootConfig, "after-interceptors")
        tryLoadClass("com.github.yoojia.web.HttpAfterLogger")?.let { clazz->
            output.add(ConfigEntry(clazz.name, InternalPriority.LOGGING_BEFORE, rootConfig.getConfig("after-logger")))
        }
        tryLoadClass("com.github.yoojia.web.Velocity")?.let { clazz->
            output.add(ConfigEntry(clazz.name, InternalPriority.TEMPLATES, rootConfig.getConfig("template")))
        }
        return output
    }

    private fun findModules(rootConfig: Config): List<ConfigEntry>{
        return findConfig(rootConfig, "modules")
    }

    private fun findConfig(rootConfig: Config, section: String): MutableList<ConfigEntry> {
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

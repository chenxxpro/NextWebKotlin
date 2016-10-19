package com.github.yoojia.web.core

import com.github.yoojia.web.Request
import com.github.yoojia.web.Response
import com.github.yoojia.web.StatusCode
import com.github.yoojia.web.http.HttpControllerHandler
import com.github.yoojia.web.interceptor.AfterHandler
import com.github.yoojia.web.interceptor.BeforeHandler
import com.github.yoojia.web.supports.InternalPriority
import com.github.yoojia.web.util.*
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths
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
object Engine {

    const val VERSION = "NextEngine/2.a.20-6 (Kotlin 1.0.3; Java 7,8)"
    private val CONFIG_FILE = "WEB-INF${File.separator}next.yml"

    private val Logger = LoggerFactory.getLogger(Engine::class.java)

    private val dispatcher = DispatchChain()
    private val contextRef = AtomicReference<Context>()

    @JvmField val kernelManager = KernelManager()

    fun init(servletContext: ServletContext) {
        start(servletContext, ConfigLoader(), RuntimeClassProvider())
    }

    fun start(servletContext: ServletContext, configProvider: ConfigProvider, classProvider: ClassProvider) {
        Logger.warn("===> NextEngine START BOOTING, Version: $VERSION")
        val start = now()
        val directory = servletContext.getRealPath("/")
        val config = configProvider.getConfig(Paths.get(directory, CONFIG_FILE))
        Logger.debug("Config-From : ${config.getStringValue(ConfigLoader.KEY_CONFIG_PATH)}")
        Logger.debug("Config-State: ${config.getStringValue(ConfigLoader.KEY_CONFIG_STATE)}")
        Logger.debug("Config-Time : ${escape(start)}ms")
        val ctx = Context(directory, config, servletContext)
        contextRef.set(ctx)
        Logger.debug("Web-Directory: ${ctx.webPath}")
        Logger.debug("Web-Context  : ${ctx.contextPath}")
        initModules(ctx, classProvider.getClasses(ctx).toMutableList(), kernelManager)
        kernelManager.withModules { module -> dispatcher.add(module) }
        Logger.debug("Loaded-Modules: ${kernelManager.moduleCount()}")
        Logger.debug("Loaded-Plugins: ${kernelManager.pluginCount()}")
        kernelManager.onCreated(ctx)
        Logger.warn("<=== NextEngine BOOT SUCCESSFUL, Boot time: ${escape(start)}ms")
    }

    fun process(req: ServletRequest, res: ServletResponse) {
        val context = contextRef.get()
        val response = Response(context, res as HttpServletResponse)
        val request = Request(context, req as HttpServletRequest)
        // default configs:
        response.setStatusCode(StatusCode.NOT_FOUND) // Default: 404
        response.putArg("app-base", request.contextPath)
        response.putArg("req-base", request.path)
        try{
            dispatcher.route(request, response)
        }catch(err: Throwable) {
            Logger.error("Error when processing request", err)
            try{
                response.sendError(err)
            }catch(stillError: Throwable) {
                Logger.error("Error when send ERROR to client", stillError)
            }
        }
    }

    fun shutdown() {
        dispatcher.clear()
        kernelManager.onDestroy()
    }

    private fun initModules(context: Context, classes: MutableList<Class<*>>, manager: KernelManager) {
        val rootConfig = context.rootConfig
        val register = fun(tag: String, module: Module, priority: Int, config: String){
            val start = now()
            val scrapClasses = module.prepare(classes)
            if (scrapClasses.isNotEmpty()) {
                classes.removeAll(scrapClasses)
            }
            Logger.debug("$tag-Prepare: ${escape(start)}ms")
            manager.register(module, priority, rootConfig.getConfig(config))
        }
        // Core modules
        register("BeforeInterceptor", BeforeHandler(classes), BeforeHandler.DEFAULT_PRIORITY, "before-interceptor")
        register("AfterInterceptor", AfterHandler(classes), AfterHandler.DEFAULT_PRIORITY, "after-interceptor")
        register("Http", HttpControllerHandler(classes), HttpControllerHandler.DEFAULT_PRIORITY, "http")

        val classLoader = getCoreClassLoader()
        // Extensions
        tryExtensionModules(context, classes, manager)
        // User.modules
        val modulesStart = now()
        val modulesConfig = rootConfig.getConfigList("modules")
        modulesConfig.forEach { config ->
            val args = parseConfigArgs(config)
            val moduleClass = loadClassByName(classLoader, args.className)
            val moduleInstance = newClassInstance<Module>(moduleClass)
            val scrapClasses = moduleInstance.prepare(classes)
            classes.removeAll(scrapClasses)
            manager.register(moduleInstance, args.priority, args.args)
        }
        if(modulesConfig.isNotEmpty()) {
            Logger.debug("User-Modules-Prepare: ${escape(modulesStart)}ms")
        }
        // User.plugins
        val pluginStart = now()
        val pluginsConfig = rootConfig.getConfigList("plugins")
        pluginsConfig.forEach { config ->
            val args = parseConfigArgs(config)
            val pluginClass = loadClassByName(classLoader, args.className)
            val pluginInstance = newClassInstance<Plugin>(pluginClass)
            manager.register(pluginInstance, args.priority, args.args)
        }
        if(pluginsConfig.isNotEmpty()) {
            Logger.debug("User-Plugins-Prepare: ${escape(pluginStart)}ms")
        }
    }

    /**
     *  - 日志： com.github.yoojia.web.LoggerHandler
     *  - 资源： com.github.yoojia.web.Assets
     *  - 模板： com.github.yoojia.web.VelocityTemplates
     */
    private fun tryExtensionModules(context: Context, out: MutableList<Class<*>>, manager: KernelManager) {
        val ifExistsThenLoad = fun(className: String, configName: String, tagName: String, priorityAction: (Int)->Int){
            val start = now()
            tryLoadClass(className)?.let { moduleClass->
                val args = parseConfigArgs(context.rootConfig.getConfig(configName))
                val moduleInstance = newClassInstance<Module>(moduleClass)
                val scrapClasses = moduleInstance.prepare(out)
                manager.register(moduleInstance, priorityAction.invoke(args.priority), args.args)
                if(scrapClasses.isNotEmpty()) {
                    out.removeAll(scrapClasses)
                    Logger.debug("$tagName-Classes: ${scrapClasses.size}")
                }
                Logger.debug("$tagName-Prepare: ${escape(start)}ms")
            }
        }

        // Logger
        ifExistsThenLoad("com.github.yoojia.web.BeforeLoggerHandler", "logger", "BeforeLogger", { InternalPriority.LOGGER_BEFORE })
        ifExistsThenLoad("com.github.yoojia.web.AfterLoggerHandler", "logger", "AfterLogger", { InternalPriority.LOGGER_AFTER })

        // Assets
        ifExistsThenLoad("com.github.yoojia.web.Assets", "assets", "Assets", { define ->
            val priority: Int
            if(define >= HttpControllerHandler.DEFAULT_PRIORITY) {
                priority = InternalPriority.ASSETS
                Logger.info("Assets.priority($define) must be < HTTP.priority(${HttpControllerHandler.DEFAULT_PRIORITY}), set to: $priority")
            }else{
                priority = define
            }
            priority
        })

        // Templates
        ifExistsThenLoad("com.github.yoojia.web.VelocityTemplates", "templates", "Templates", { define ->
            val priority: Int
            if(define <= HttpControllerHandler.DEFAULT_PRIORITY) {
                priority = InternalPriority.TEMPLATES
                Logger.info("Templates.priority($define) must be > HTTP.priority(${HttpControllerHandler.DEFAULT_PRIORITY}), set to: $priority")
            }else{
                priority = define
            }
            priority
        })
    }

    /**
     * 解析配置条目
     */
    private fun parseConfigArgs(config: Config): ConfigStruct {
        return ConfigStruct(config.getStringValue("class"),
                config.getIntValue("priority"),
                config.getConfig("args"))
    }

    /**
     * 配置参数数据包装.不使用Triple的是为使得参数意义更新清晰.
     */
    private data class ConfigStruct(val className: String, val priority: Int, val args: Config)

}

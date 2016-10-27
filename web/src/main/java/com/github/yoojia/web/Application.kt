package com.github.yoojia.web

import com.github.yoojia.web.http.HttpHandler
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
object Application {

    const val VERSION = "NextEngine/2.21-ALPHA (Kotlin 1.0.4; Java 7/8;)"

    private val CONFIG_FILE = "WEB-INF${File.separator}next.yml"

    private val Logger = LoggerFactory.getLogger(Application::class.java)

    private val router = Router()
    private val appContext = AtomicReference<Context>()

    private val kernels = KernelManager()

    fun setup(servletContext: ServletContext) {
        startup(servletContext, YamlConfigLoader(), RuntimeClassProvider())
    }

    fun startup(servletContext: ServletContext, configProvider: ConfigProvider, classProvider: ClassProvider) {
        Logger.warn("===> NextEngine START BOOTING, Version: $VERSION")
        val start = now()
        val path = servletContext.getRealPath("/")
        val config = configProvider.getConfig(Paths.get(path, CONFIG_FILE))
        val ctx = Context(path, config, servletContext)
        appContext.set(ctx)
        initKernel(classProvider)
        Logger.debug("Config-From : ${config.getStringValue(YamlConfigLoader.KEY_CONFIG_PATH)}")
        Logger.debug("Config-State: ${config.getStringValue(YamlConfigLoader.KEY_CONFIG_STATE)}")
        Logger.debug("Config-Time : ${escape(start)}ms")
        Logger.debug("App-Path: ${ctx.webPath}")
        Logger.debug("App-Context  : ${ctx.contextPath}")
        Logger.debug("Modules-Loaded: ${kernels.moduleCount()}")
        Logger.debug("Plugins-Loaded: ${kernels.pluginCount()}")
        Logger.warn("<=== NextEngine BOOT SUCCESSFUL, Boot time: ${escape(start)}ms")
    }

    fun process(req: ServletRequest, res: ServletResponse) {
        val context = appContext.get()
        // 接收到每个客户端请求时，创建 Request 和 Response 临时对象，生命周期只限于请求过程。
        val request = Request(context, req as HttpServletRequest)
        val response = Response(context, res as HttpServletResponse)
        // default configs:
        response.setStatusCode(StatusCode.NOT_FOUND) // Default: 404
        response.putArg("app-context", request.contextPath)
        response.putArg("request-path", request.path)
        try{
            router.route(request, response)
        }catch(err: Throwable) {
            Logger.error("Error when processing request", err)
            try{
                response.sendError(err)
            }catch(still: Throwable) {
                Logger.error("Error when send ERROR to client", still)
            }
        }
    }

    fun shutdown() {
        router.shutdown()
        kernels.destroy()
    }

    private fun initKernel(classProvider: ClassProvider){
        val ctx = appContext.get()
        instances(ctx, classProvider.getClasses(ctx).toMutableList(), kernels)
        kernels.modules { module -> router.add(module) }
        kernels.created(ctx)
    }

    private fun instances(context: Context, classes: MutableList<Class<*>>, manager: KernelManager) {
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
        register("Http", HttpHandler(classes), HttpHandler.DEFAULT_PRIORITY, "http")

        val classLoader = getCoreClassLoader()
        // Extensions
        tryExtensionModules(context, classes, manager)
        // User.modules
        val modulesStart = now()
        val modulesConfig = rootConfig.getConfigList("modules")
        modulesConfig.forEach { conf ->
            val args = parseConfigArgs(conf)
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
        pluginsConfig.forEach { conf ->
            val args = parseConfigArgs(conf)
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
            if(define >= HttpHandler.DEFAULT_PRIORITY) {
                priority = InternalPriority.ASSETS
                Logger.info("Assets.priority($define) must be < HTTP.priority(${HttpHandler.DEFAULT_PRIORITY}), set to: $priority")
            }else{
                priority = define
            }
            priority
        })

        // Templates
        ifExistsThenLoad("com.github.yoojia.web.VelocityTemplates", "templates", "Templates", { define ->
            val priority: Int
            if(define <= HttpHandler.DEFAULT_PRIORITY) {
                priority = InternalPriority.TEMPLATES
                Logger.info("Templates.priority($define) must be > HTTP.priority(${HttpHandler.DEFAULT_PRIORITY}), set to: $priority")
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

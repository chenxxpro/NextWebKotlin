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

    const val VERSION = "NextEngine/2.a.4 (Kotlin 1.0.2; Java 7/8)"
    private val CONFIG_FILE = "WEB-INF${File.separator}next.yml"

    private val Logger = LoggerFactory.getLogger(Engine::class.java)

    private val dispatcher = DispatchChain()
    private val kernelManager = KernelManager()
    private val contextRef = AtomicReference<Context>()

    fun boot(servletContext: ServletContext, classProvider: ClassProvider) {
        if("main".notEquals(Thread.currentThread().name)) {
            throw IllegalStateException("Engine must boot on <MAIN> thread")
        }
        Logger.debug("===> NextEngine BOOTING")
        Logger.debug("Engine-Version: $VERSION")
        val webPath = servletContext.getRealPath("/")
        val config = loadConfig(webPath)

        val _start = now()
        val ctx = Context(webPath, config, servletContext)
        contextRef.set(ctx)
        Logger.debug("Web-Directory: ${ctx.webPath}")
        Logger.debug("Web-Context: ${ctx.contextPath}")

        initModules(ctx, classProvider.get(ctx).toMutableList())
        kernelManager.allModules { module ->
            dispatcher.add(module)
        }
        Logger.debug("Loaded-Modules: ${kernelManager.moduleCount()}")
        Logger.debug("Loaded-Plugins: ${kernelManager.pluginCount()}")

        kernelManager.onCreated(ctx)

        Logger.debug("Boot-Time: ${escape(_start)}ms")
        Logger.debug("<=== NextEngine BOOT SUCCESSFUL")
    }

    fun process(req: ServletRequest, res: ServletResponse) {
        val context = contextRef.get()
        val request = Request(context, req as HttpServletRequest)
        val response = Response(context, res as HttpServletResponse)
        // Default: 404
        response.setStatusCode(StatusCode.NOT_FOUND)
        try{
            dispatcher.route(request, response)
        }catch(err: Throwable) {
            Logger.error("Error when processing request", err)
            try{
                response.sendError(err)
            }catch(stillError: Throwable) {
                Logger.error("Error when send ERROR to client",stillError)
            }
        }
    }

    fun shutdown() {
        dispatcher.clear()
        kernelManager.onDestroy()
    }

    private fun loadConfig(webPath: String): Config{
        val start = now()
        val config = loadConfig(Paths.get(webPath, CONFIG_FILE))
        Logger.debug("Config-File: ${config.getString(KEY_CONFIG_PATH)}")
        Logger.debug("Load-State: ${config.getString(KEY_CONFIG_STATE)}")
        Logger.debug("Load-Time: ${escape(start)}ms")
        return config
    }

    private fun initModules(context: Context, classes: MutableList<Class<*>>) {
        val rootConfig = context.config;

        val register = fun(tag: String, module: Module, priority: Int, config: String){
            val start = now()
            val preUsed = module.prepare(classes);
            classes.removeAll(preUsed)
            Logger.debug("$tag-Prepare: ${escape(start)}ms")
            kernelManager.register(module, priority, rootConfig.getConfig(config))
        }

        // Kernel
        register("BeforeInterceptor", BeforeHandler(classes), BeforeHandler.DEFAULT_PRIORITY, "before-interceptor")
        register("AfterInterceptor", AfterHandler(classes), AfterHandler.DEFAULT_PRIORITY, "after-interceptor")
        register("Http", HttpControllerHandler(classes), HttpControllerHandler.DEFAULT_PRIORITY, "http")

        tryBuildInModules(context, classes)

        // User
        val classLoader = getClassLoader()
        val modulesStart = now()
        val modules = rootConfig.getConfigList("modules")
        modules.forEach { config ->
            val args = parseConfig(config)
            val module = newClassInstance<Module>(loadClassByName(classLoader, args.className))
            val moduleUsed = module.prepare(classes)
            classes.removeAll(moduleUsed)
            kernelManager.register(module, args.priority, args.args)
        }
        if(modules.isNotEmpty()) {
            Logger.debug("User-Modules-Prepare: ${escape(modulesStart)}ms")
        }

        val pluginStart = now()
        val plugins = rootConfig.getConfigList("plugins")
        plugins.forEach { config ->
            val args = parseConfig(config)
            val plugin = newClassInstance<Plugin>(loadClassByName(classLoader, args.className))
            kernelManager.register(plugin, args.priority, args.args)
        }
        if(plugins.isNotEmpty()) {
            Logger.debug("User-Plugins-Prepare: ${escape(pluginStart)}ms")
        }
    }

    /**
        - 资源： com.github.yoojia.web.Assets
        - 模板： com.github.yoojia.web.VelocityTemplates
    */
    private fun tryBuildInModules(context: Context, classes: MutableList<Class<*>>) {
        val classLoader = getClassLoader()
        val httpPriority = HttpControllerHandler.DEFAULT_PRIORITY
        val ifExistsThenLoad = fun(className: String, configName: String, tagName: String, priorityAction: (Int)->Int){
            if(classExists(className)){
                val start = now()
                val args = parseConfig(context.config.getConfig(configName))
                val module = newClassInstance<Module>(loadClassByName(classLoader, className))
                val used = module.prepare(classes)
                classes.removeAll(used)
                kernelManager.register(module, priorityAction.invoke(args.priority), args.args)
                Logger.debug("$tagName-Classes: ${used.size}")
                Logger.debug("$tagName-Prepare: ${escape(start)}ms")
            }
        }

        // Assets
        ifExistsThenLoad("com.github.yoojia.web.Assets", "assets", "Assets", { define ->
            val priority: Int
            if(define >= HttpControllerHandler.DEFAULT_PRIORITY) {
                priority = InternalPriority.ASSETS
                Logger.info("Assets.priority($define) must be < HTTP.priority($httpPriority), set to: $priority")
            }else{
                priority = define
            }
            priority
        })

        // Templates
        ifExistsThenLoad("com.github.yoojia.web.VelocityTemplates", "templates", "Templates", { define ->
            val priority: Int
            if(define <= HttpControllerHandler.DEFAULT_PRIORITY) {
                priority = InternalPriority.VELOCITY
                Logger.info("Templates.priority($define) must be > HTTP.priority($httpPriority), set to: $priority")
            }else{
                priority = define
            }
            priority
        })
    }

    private fun classExists(name: String): Boolean {
        try{
            Class.forName(name)
            return true
        }catch(err: Exception) {
            return false
        }
    }

}

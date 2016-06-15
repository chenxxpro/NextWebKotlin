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
 * NextWeb framework Engine
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class Engine {

    companion object {
        
        private val Logger = LoggerFactory.getLogger(Engine::class.java)
        
        const val VERSION = "NextEngine/2.5 (Kotlin 1.0.2; Java 7)"
        private val CONFIG_FILE = "WEB-INF${File.separator}next.yml"
    }

    private val dispatchChain = DispatchChain()
    private val kernelManager = KernelManager()
    private val contextRef = AtomicReference<Context>()

    fun start(servletContext: ServletContext, classProvider: ClassProvider) {
        val start = now()
        Logger.debug("--> NextEngine starting")
        Logger.debug("Engine-Version: $VERSION")
        val webPath = servletContext.getRealPath("/")
        val config = loadConfig(Paths.get(webPath, CONFIG_FILE))
        Logger.debug("Config-File: ${config.getString(KEY_CONFIG_PATH)}")
        Logger.debug("Config-Load-State: ${config.getString(KEY_CONFIG_STATE)}")
        Logger.debug("Config-Load-Time: ${escape(start)}ms")
        val ctx = Context(webPath, config, servletContext)
        contextRef.set(ctx)
        Logger.debug("Web-Directory: ${ctx.webPath}")
        Logger.debug("Web-Context: ${ctx.contextPath}")
        // 初始化所有需要加载的模块类
        initModules(ctx, classProvider.get(ctx).toMutableList())
        // 核心模块/插件注册到Chain中
        kernelManager.allModules { module ->
            dispatchChain.add(module)
        }
        Logger.debug("Loaded-Modules: ${kernelManager.moduleCount()}")
        Logger.debug("Loaded-Plugins: ${kernelManager.pluginCount()}")
        // 启动全部内核模块
        kernelManager.onCreated(ctx)
        Logger.debug("Engine-Boot: ${escape(start)}ms")
        Logger.debug("<-- NextEngine started successfully")
    }

    fun process(req: ServletRequest, res: ServletResponse) {
        val context = contextRef.get()
        // 默认情况下，HTTP状态码为 404 NOT FOUND。
        // 在不同模块中有不同的默认HTTP状态码逻辑，由各个模块定夺。
        val response = Response(context, res as HttpServletResponse)
        val request = Request(context, req as HttpServletRequest)
        Logger.info("NextEngine-Accepted: ${request.path}")
        response.setStatusCode(StatusCode.NOT_FOUND)
        try{
            dispatchChain.process(request, response)
        }catch(err: Throwable) {
            Logger.error("Error when processing request", err)
            try{
                response.sendError(err)
            }catch(stillError: Throwable) {
                Logger.error("Error when send ERROR to client",stillError)
            }
        }
    }

    fun stop() {
        dispatchChain.clear()
        kernelManager.onDestroy()
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
        // Kernel modules
        register("BeforeInterceptor", BeforeHandler(classes), BeforeHandler.DEFAULT_PRIORITY, "before-interceptor")
        register("AfterInterceptor", AfterHandler(classes), AfterHandler.DEFAULT_PRIORITY, "after-interceptor")
        register("Http", HttpControllerHandler(classes), HttpControllerHandler.DEFAULT_PRIORITY, "http")
        // Build-in
        tryBuildInModules(context, classes)
        // User modules
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
        Logger.debug("User-Modules-Prepare: ${escape(modulesStart)}ms")
        // 从配置文件中加载用户插件
        val pluginStart = now()
        val pluginConfigs = rootConfig.getConfigList("plugins")
        pluginConfigs.forEach { config ->
            val args = parseConfig(config)
            val plugin = newClassInstance<Plugin>(loadClassByName(classLoader, args.className))
            kernelManager.register(plugin, args.priority, args.args)
        }
        Logger.debug("User-Plugins-Prepare: ${escape(pluginStart)}ms")
    }

    /**
        尝试加载内部实现模块：
        - 上传： com.github.yoojia.web.Uploads
        - 资源： com.github.yoojia.web.Assets
        - 下载： com.github.yoojia.web.Downloads
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
        // Uploads
        ifExistsThenLoad("com.github.yoojia.web.Uploads", "uploads", "Uploads", { define ->
            val priority: Int
            if(define >= HttpControllerHandler.DEFAULT_PRIORITY) {
                priority = InternalPriority.UPLOADS
                Logger.info("Uploads.priority($define) must be < HTTP.priority($httpPriority), set to: $priority")
            }else{
                priority = define
            }
            priority
        })
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
        // Downloads
        ifExistsThenLoad("com.github.yoojia.web.Downloads", "downloads", "Downloads", { define ->
            val priority: Int
            if(define <= HttpControllerHandler.DEFAULT_PRIORITY) {
                priority = InternalPriority.DOWNLOADS
                Logger.info("Downloads.priority($define) must be > HTTP.priority($httpPriority), set to: $priority")
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

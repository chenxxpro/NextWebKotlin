package com.github.yoojia.web.kernel

import com.github.yoojia.web.Request
import com.github.yoojia.web.Response
import com.github.yoojia.web.StatusCode
import com.github.yoojia.web.http.HttpHandler
import com.github.yoojia.web.interceptor.AfterHandler
import com.github.yoojia.web.interceptor.BeforeHandler
import com.github.yoojia.web.supports.*
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
        const val VERSION = "NextEngine/2.2 (Kotlin 1.0.2; Java 7)"
    }

    private val mDispatchChain = DispatchChain()
    private val mKernelManager = KernelManager()
    private val mContext = AtomicReference<Context>()

    fun start(servletContext: ServletContext, classProvider: ClassProvider) {
        Logger.d("--> NextEngine starting")
        Logger.d("Engine-Version: $VERSION")
        val engineStart = now()
        val context = Context(servletContext)
        mContext.set(context)
        Logger.d("Web-Directory: ${context.webPath}")
        Logger.d("Web-Context: ${servletContext.contextPath}")
        // 扫描
        initModules(context, classProvider.get().toMutableList())
        // 所有Module注册到Chain中
        mKernelManager.modulesForEach { module ->
            mDispatchChain.add(module)
        }
        Logger.d("Loaded-Modules: ${mKernelManager.moduleCount()}")
        Logger.d("Loaded-Plugins: ${mKernelManager.pluginCount()}")
        // 启动全部内核模块
        mKernelManager.onCreated(context)
        Logger.d("Engine-Boot: ${escape(engineStart)}ms")
        Logger.d("<-- NextEngine started successfully")
    }

    fun process(req: ServletRequest, res: ServletResponse) {
        val context = mContext.get()
        // 默认情况下，HTTP状态码为404。在不同模块中有不同的默认HTTP状态码逻辑，由各个模块定夺。
        val response = Response(context, res as HttpServletResponse)
        val request = Request(context, req as HttpServletRequest)
        Logger.vv("NextEngine-Accepted: ${request.path}")
        response.setStatusCode(StatusCode.NOT_FOUND)
        try{
            mDispatchChain.process(request, response)
        }catch(err: Throwable) {
            // 尝试发送错误消息 给客户端。
            // 若依然失败时，服务端已无法处理，直接打印出错信息到日志
            Logger.e(err)
            try{
                response.sendError(err)
            }catch(stillError: Throwable) {
                Logger.e(stillError)
            }
        }
    }

    fun stop() {
        mDispatchChain.clear()
        mKernelManager.onDestroy()
    }

    private fun initModules(context: Context, classes: MutableList<Class<*>>) {
        val rootConfig = context.config;

        val register = fun(tag: String, module: Module, priority: Int, config: String){
            val start = now()
            val preUsed = module.prepare(classes);
            classes.removeAll(preUsed)
            Logger.d("$tag-Prepare: ${escape(start)}ms")
            mKernelManager.register(module, priority, rootConfig.getConfig(config))
        }
        // Kernel modules
        register("BeforeInterceptor", BeforeHandler(classes), BeforeHandler.DEFAULT_PRIORITY, "before-interceptor")
        register("AfterInterceptor", AfterHandler(classes), AfterHandler.DEFAULT_PRIORITY, "after-interceptor")
        register("Http", HttpHandler(classes), HttpHandler.DEFAULT_PRIORITY, "http")
        // Build-in
        tryRegisterBuildInModules(context, classes)
        // User modules
        val classLoader = getClassLoader()
        val modulesStart = now()
        val modules = rootConfig.getConfigList("modules")
        modules.forEach { config ->
            val args = parseConfig(config)
            val module = newClassInstance<Module>(loadClassByName(classLoader, args.className))
            val moduleUsed = module.prepare(classes)
            classes.removeAll(moduleUsed)
            mKernelManager.register(module, args.priority, args.args)
        }
        Logger.d("User-Modules-Prepare: ${escape(modulesStart)}ms")
        // 从配置文件中加载用户插件
        val pluginStart = now()
        val pluginConfigs = rootConfig.getConfigList("plugins")
        pluginConfigs.forEach { config ->
            val args = parseConfig(config)
            val plugin = newClassInstance<Plugin>(loadClassByName(classLoader, args.className))
            mKernelManager.register(plugin, args.priority, args.args)
        }
        Logger.d("User-Plugins-Prepare: ${escape(pluginStart)}ms")
    }

    /**
        尝试加载内部实现模块：
        - 上传： com.github.yoojia.web.Uploads
        - 资源： com.github.yoojia.web.Assets
        - 下载： com.github.yoojia.web.Downloads
        - 模板： com.github.yoojia.web.VelocityTemplates
    */
    private fun tryRegisterBuildInModules(context: Context, classes: MutableList<Class<*>>) {
        val classLoader = getClassLoader()
        val httpPriority = HttpHandler.DEFAULT_PRIORITY
        val load = fun(className: String, configName: String, tagName: String, priority: (Int)->Int){
            val start = now()
            val args = parseConfig(context.config.getConfig(configName))
            val module = newClassInstance<Module>(loadClassByName(classLoader, className))
            val used = module.prepare(classes)
            classes.removeAll(used)
            mKernelManager.register(module, priority.invoke(args.priority), args.args)
            Logger.d("$tagName-Classes: ${used.size}")
            Logger.d("$tagName-Prepare: ${escape(start)}ms")
        }
        // Uploads
        val uploadsClassName = "com.github.yoojia.web.Uploads"
        if(classExists(uploadsClassName)){
            load(uploadsClassName, "uploads", "Uploads", { define ->
                val priority:Int
                if(define >= HttpHandler.DEFAULT_PRIORITY) {
                    priority = InternalPriority.UPLOADS
                    Logger.v("Uploads.priority($define) must be < HTTP.priority($httpPriority), set to: $priority")
                }else{
                    priority = define
                }
                priority
            })
        }
        // Assets
        val assetsClassName = "com.github.yoojia.web.Assets"
        if(classExists(assetsClassName)){
            load(assetsClassName, "assets", "Assets", { define ->
                val priority:Int
                if(define >= HttpHandler.DEFAULT_PRIORITY) {
                    priority = InternalPriority.ASSETS
                    Logger.v("Assets.priority($define) must be < HTTP.priority($httpPriority), set to: $priority")
                }else{
                    priority = define
                }
                priority
            })
        }
        // Downloads
        val downloadsClassName = "com.github.yoojia.web.Downloads"
        if(classExists(downloadsClassName)){
            load(downloadsClassName, "downloads", "Downloads", { define ->
                val priority:Int
                if(define <= HttpHandler.DEFAULT_PRIORITY) {
                    priority = InternalPriority.DOWNLOADS
                    Logger.v("Downloads.priority($define) must be > HTTP.priority($httpPriority), set to: $priority")
                }else{
                    priority = define
                }
                priority
            })
        }
        // Templates
        val templatesClassName = "com.github.yoojia.web.VelocityTemplates"
        if(classExists(templatesClassName)){
            load(templatesClassName, "templates", "Templates", { define ->
                val priority:Int
                if(define <= HttpHandler.DEFAULT_PRIORITY) {
                    priority = InternalPriority.VELOCITY
                    Logger.v("Templates.priority($define) must be > HTTP.priority($httpPriority), set to: $priority")
                }else{
                    priority = define
                }
                priority
            })
        }
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

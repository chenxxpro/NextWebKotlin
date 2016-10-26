package com.github.yoojia.web

import com.github.yoojia.web.Config
import com.github.yoojia.web.Context
import com.github.yoojia.web.Router
import com.github.yoojia.web.Module
import com.github.yoojia.web.supports.Comparator
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.util.*

/**
 * @author Yoojia Chen (yoojia.chen@gmail.com)
 * @since 2.0
 */
class Assets : Module {

    private val define = ArrayList<Comparator>()

    companion object {
        private val Logger = LoggerFactory.getLogger(Assets::class.java)
    }

    override fun onCreated(context: Context, config: Config) {
        config.getTypedList<String>("uri-mapping").forEach { uri->
            val path = if(uri.endsWith("/")) "$uri/*" else uri
            Logger.debug("Assets-URI-Define: $path")
            define.add(Comparator.createDefine("ALL", path))
        }
    }

    override fun process(request: Request, response: Response, router: Router) {
        if(match(request)){
            val local = request.context.resolvePath(request.path)
            if(Files.exists(local)) {
                response.setStatusCode(StatusCode.OK)
                val path = local.toString()
                request.servletRequest.servletContext.getMimeType(path)?.let{ mimeType ->
                    response.setContextType(mimeType)
                }
                TransferAdapter(local).dispatch(request, response)
            }else{
                response.setStatusCode(StatusCode.NOT_FOUND)
            }
        }else{
            router.next(request, response, router)
        }
    }

    private fun match(request: Request): Boolean {
        define.forEach { define->
            if(request.comparator.isMatchDefine(define)) {
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        define.clear()
    }
}
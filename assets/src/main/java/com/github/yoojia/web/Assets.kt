package com.github.yoojia.web

import com.github.yoojia.web.core.Config
import com.github.yoojia.web.core.Context
import com.github.yoojia.web.core.DispatchChain
import com.github.yoojia.web.core.Module
import com.github.yoojia.web.supports.Comparator
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.util.*

/**
 * @author Yoojia Chen (yoojia.chen@gmail.com)
 * @since 2.0
 */
class Assets : Module {

    private val mAssetsDefine = ArrayList<Comparator>()

    companion object {
        private val Logger = LoggerFactory.getLogger(Assets::class.java)
    }

    override fun onCreated(context: Context, config: Config) {
        config.getTypedList<String>("uri-mapping").forEach { uri->
            val path = if(uri.endsWith("/")) "$uri/*" else uri
            Logger.debug("Assets-URI-Define: $path")
            mAssetsDefine.add(Comparator.createDefine("ALL", path))
        }
    }

    override fun process(request: Request, response: Response, dispatch: DispatchChain) {
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
            dispatch.next(request, response, dispatch)
        }
    }

    private fun match(request: Request): Boolean {
        mAssetsDefine.forEach { define->
            if(request.comparator.isMatchDefine(define)) {
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        mAssetsDefine.clear()
    }
}
package com.github.yoojia.web

import com.github.yoojia.web.core.*
import com.github.yoojia.web.supports.UriSegment
import com.github.yoojia.web.util.isUriResourceMatched
import com.github.yoojia.web.util.splitUri
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.util.*

/**
 * @author Yoojia Chen (yoojia.chen@gmail.com)
 * @since 2.0
 */
class Assets : Module {

    private var mForwardIfMatched = false

    private val mAssetsDefine = ArrayList<List<String>>()

    companion object {
        private val Logger = LoggerFactory.getLogger(Assets::class.java)
    }

    override fun onCreated(context: Context, config: Config) {
        mForwardIfMatched = config.getBoolean("forward")
        Logger.debug("Assets-Forward: $mForwardIfMatched")
        mAssetsDefine.clear()
        for(uri in config.getTypedList<String>("uri-mapping")) {
            val path = if(uri.endsWith("/*")) uri else uri + "/*"
            Logger.debug("Assets-URI-Define: $path")
            mAssetsDefine.add(splitUri(path))
        }
    }

    override fun process(request: Request, response: Response, dispatch: DispatchChain) {
        if(match(request.resources)){
            val local = request.context.resolvePath(request.path)
            if(Files.exists(local)) {
                response.setStatusCode(StatusCode.OK)
                val path = local.toString()
                request.raw.servletContext.getMimeType(path)?.let{ mimeType ->
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

    private fun match(sources: List<String>): Boolean {
        //request: /assets/js/boot.js
        val request = sources.map { UriSegment(it) }
        for(asset in mAssetsDefine) {
            //define: /assets/js/*
            val define = asset.map { UriSegment(it) }
            return UriSegment.isRequestMatchDefine(request, define)
        }
        return false
    }

    override fun onDestroy() {
        mAssetsDefine.clear()
    }
}
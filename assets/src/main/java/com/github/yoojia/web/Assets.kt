package com.github.yoojia.web

import com.github.yoojia.web.core.Config
import com.github.yoojia.web.core.Context
import com.github.yoojia.web.core.DispatchChain
import com.github.yoojia.web.core.Module
import com.github.yoojia.web.supports.Logger
import com.github.yoojia.web.util.isUriResourceMatched
import com.github.yoojia.web.util.splitUri
import java.nio.file.Files
import java.util.*

/**
 * @author Yoojia Chen (yoojia.chen@gmail.com)
 * @since 2.0
 */
class Assets : Module {

    private var mForwardIfMatched = false

    private val mAssetsDefine = ArrayList<List<String>>()

    override fun onCreated(context: Context, config: Config) {
        mForwardIfMatched = config.getBoolean("forward")
        Logger.d("Assets-Forward: $mForwardIfMatched")
        mAssetsDefine.clear()
        for(uri in config.getTypedList<String>("uri-mapping")) {
            val path = if(uri.endsWith("/*")) uri else uri + "/*"
            Logger.d("Assets-URI-Define: $path")
            mAssetsDefine.add(splitUri(path))
        }
    }

    override fun process(request: Request, response: Response, dispatch: DispatchChain) {
        if(matched(request.resources)){
            val local = request.context.resolvePath(request.path)
            if(Files.exists(local)) {
                response.setStatusCode(StatusCode.OK)
                response.setContextType(request.raw.servletContext.getMimeType(local.toString()))
                TransferAdapter(local).dispatch(request, response)
            }else{
                response.setStatusCode(StatusCode.NOT_FOUND)
            }
        }else{
            dispatch.next(request, response, dispatch)
        }
    }

    private fun matched(request: List<String>): Boolean {
        for(define in mAssetsDefine) {
            if(isUriResourceMatched(request, define)) {
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        mAssetsDefine.clear()
    }
}
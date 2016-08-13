package com.github.yoojia.web

import com.github.yoojia.web.core.Config
import com.github.yoojia.web.core.Context
import com.github.yoojia.web.core.DispatchChain
import com.github.yoojia.web.core.Module
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import org.apache.velocity.app.VelocityEngine
import org.slf4j.LoggerFactory
import java.io.StringWriter
import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class VelocityTemplates : Module {

    private val velocity = VelocityEngine()

    companion object {
        private val Logger = LoggerFactory.getLogger(VelocityTemplates::class.java)
    }

    override fun onCreated(context: Context, config: Config) {
        val path = context.resolvePath("/templates")
        val conf = Properties()
        conf.setProperty("resource.loader", "file")
        conf.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader")
        conf.setProperty("file.resource.loader.path", path.toString())
        conf.setProperty("file.resource.loader.cache", "true")
        conf.setProperty("file.resource.loader.modificationCheckInterval", "2")
        conf.setProperty(Velocity.ENCODING_DEFAULT, "UTF-8")
        conf.setProperty(Velocity.INPUT_ENCODING, "UTF-8")
        conf.setProperty(Velocity.OUTPUT_ENCODING, "UTF-8")
        velocity.init(conf)
    }

    override fun onDestroy() {
        // NOP
    }

    @Throws(Exception::class)
    override fun process(request: Request, response: Response, dispatch: DispatchChain) {
        val name = response.args[Response.TEMPLATE_NAME]
        if(name != null && name is String && name.isNotEmpty()) {
            Logger.trace("Template-Module-Processing: ${request.path}, template: $name")
            if ( ! velocity.resourceExists(name)) {
                throw RuntimeException("Template resource($name) not exists !")
            }
            val output = StringWriter()
            velocity.getTemplate(name)
                    .merge(VelocityContext(response.args), output)
            response.sendHtml(output.toString())
        }
        dispatch.next(request, response, dispatch)
    }
}
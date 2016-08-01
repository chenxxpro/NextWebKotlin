package com.github.yoojia.web.core

import com.github.yoojia.web.supports.Filter
import com.github.yoojia.web.util.*
import org.slf4j.LoggerFactory
import java.nio.file.Paths
import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
internal class RuntimeClassProvider : ClassProvider {

    companion object{

        private val Logger = LoggerFactory.getLogger(RuntimeClassProvider::class.java)

        private val DEFAULT_SYSTEM_CLASSES = arrayOf(
                "com.github.yoojia.web.",
                "java.",
                "javax.",
                "org.xml.",
                "org.w3c."
        )
    }

    override fun get(context: Context): List<Class<*>> {
        val start = now()
        val filter = object : Filter<String> {
            override fun accept(value: String): Boolean {
                DEFAULT_SYSTEM_CLASSES.forEach {
                    if(value.startsWith(it)) return false
                }
                return true
            }
        }
        val classPath = Paths.get(Engine::class.java.getResource("/").toURI())
        Logger.debug("Class-Path: $classPath")
        val classPathClasses = findRuntimeNames(classPath, filter)
        val jarClasses = findJarClassNames(filter)
        val classes = ArrayList<Class<*>>(loadClassesByNames(classPathClasses.concat(jarClasses)))
        Logger.debug("Class-Count: ${classes.size}")
        Logger.debug("Scan-Time: ${escape(start)}ms")
        return classes
    }

}
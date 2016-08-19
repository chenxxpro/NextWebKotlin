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
        val runtimeConfig = context.rootConfig.getConfig("runtime-classes")
        // Accepts
        val acceptStarts = runtimeConfig.getTypedList<String>("accept-starts")
        val hasAcceptStarts = acceptStarts.isNotEmpty()
        if(hasAcceptStarts) acceptStarts.forEach { startName->
            Logger.debug("Accept-Starts: $startName")
        }

        val acceptClass = runtimeConfig.getTypedList<String>("accept-classes")
        val hasAcceptClass = acceptClass.isNotEmpty()
        if(hasAcceptClass) acceptClass.forEach { className->
            Logger.debug("Accept-Class: $className")
        }

        val acceptEnds = runtimeConfig.getTypedList<String>("accept-ends")
        val hasAcceptEnds = acceptEnds.isNotEmpty()
        if(hasAcceptEnds) acceptEnds.forEach { endName->
            Logger.debug("Accept-Ends: $endName")
        }

        // Ignore

        val ignoreStarts = runtimeConfig.getTypedList<String>("ignore-starts")
        val hasIgnoreStarts = ignoreStarts.isNotEmpty()
        if(hasIgnoreStarts) ignoreStarts.forEach { startName->
            Logger.debug("Ignore-Starts: $startName")
        }

        val ignoreClass = runtimeConfig.getTypedList<String>("ignore-classes")
        val hasIgnoreClass = ignoreClass.isNotEmpty()
        if(hasIgnoreClass) ignoreClass.forEach { className->
            Logger.debug("Ignore-Class: $className")
        }

        val ignoreEnds = runtimeConfig.getTypedList<String>("ignore-ends")
        val hasIgnoreEnds = ignoreEnds.isNotEmpty()
        if(hasIgnoreEnds) ignoreEnds.forEach { endName->
            Logger.debug("Ignore-Ends: $endName")
        }

        val filter = object : Filter<String> {
            override fun accept(className: String): Boolean {
                DEFAULT_SYSTEM_CLASSES.forEach {
                    if(className.startsWith(it)) return false
                }
                // Accepts
                if(hasAcceptStarts) acceptStarts.forEach { startName->
                    if(className.startsWith(startName)) return true
                }
                if(hasAcceptClass) acceptClass.forEach { className->
                    if(className.equals(className)) return true
                }
                if(hasAcceptEnds) acceptEnds.forEach { endName->
                    if(className.endsWith(endName)) return true
                }
                // Ignore
                if(hasIgnoreStarts) ignoreStarts.forEach { startName->
                    if(className.startsWith(startName)) return false
                }
                if(hasIgnoreClass) ignoreClass.forEach { className->
                    if(className.equals(className)) return false
                }
                if (hasIgnoreEnds) ignoreEnds.forEach { endName->
                    if(className.endsWith(endName)) return false
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
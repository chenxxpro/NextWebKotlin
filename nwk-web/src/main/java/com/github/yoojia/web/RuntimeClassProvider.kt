package com.github.yoojia.web

import com.github.yoojia.web.lang.*
import com.github.yoojia.web.supports.Filter
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

        private val IGNORE_PACKAGES = arrayOf(
                "com.github.yoojia.web.",
                "java.",
                "javax.",
                "org.xml.",
                "org.w3c."
        )
    }

    override fun getClasses(context: Context): List<Class<*>> {
        val start = now()
        val runtimeConfig = context.rootConfig.getConfig("runtime-classes")
        // Accepts
        val acceptStarts = runtimeConfig.getTypedList<String>("accept-starts")
        val hasAcceptStarts = acceptStarts.isNotEmpty()
        if(hasAcceptStarts) acceptStarts.forEach { startName->
            Logger.info("Accept-Starts: $startName")
        }

        val acceptClass = runtimeConfig.getTypedList<String>("accept-classes")
        val hasAcceptClass = acceptClass.isNotEmpty()
        if(hasAcceptClass) acceptClass.forEach { className->
            Logger.info("Accept-Class: $className")
        }

        val acceptEnds = runtimeConfig.getTypedList<String>("accept-ends")
        val hasAcceptEnds = acceptEnds.isNotEmpty()
        if(hasAcceptEnds) acceptEnds.forEach { endName->
            Logger.info("Accept-Ends: $endName")
        }

        // Ignore
        val ignoreStarts = runtimeConfig.getTypedList<String>("ignore-starts")
        val hasIgnoreStarts = ignoreStarts.isNotEmpty()
        if(hasIgnoreStarts) ignoreStarts.forEach { startName->
            Logger.info("Ignore-Starts: $startName")
        }

        val ignoreClass = runtimeConfig.getTypedList<String>("ignore-classes")
        val hasIgnoreClass = ignoreClass.isNotEmpty()
        if(hasIgnoreClass) ignoreClass.forEach { className->
            Logger.info("Ignore-Class: $className")
        }

        val ignoreEnds = runtimeConfig.getTypedList<String>("ignore-ends")
        val hasIgnoreEnds = ignoreEnds.isNotEmpty()
        if(hasIgnoreEnds) ignoreEnds.forEach { endName->
            Logger.info("Ignore-Ends: $endName")
        }

        val filter = object : Filter<String> {
            override fun accept(className: String): Boolean {
                IGNORE_PACKAGES.forEach {
                    if(className.startsWith(it)) return false
                }
                // Accepts
                if(hasAcceptStarts) acceptStarts.forEach { start->
                    if(className.startsWith(start)) return true
                }
                if(hasAcceptClass) acceptClass.forEach { name->
                    if(className == name) return true
                }
                if(hasAcceptEnds) acceptEnds.forEach { end->
                    if(className.endsWith(end)) return true
                }
                // Ignore
                if(hasIgnoreStarts) ignoreStarts.forEach { start->
                    if(className.startsWith(start)) return false
                }
                if(hasIgnoreClass) ignoreClass.forEach { name->
                    if(className == name) return false
                }
                if (hasIgnoreEnds) ignoreEnds.forEach { end->
                    if(className.endsWith(end)) return false
                }
                return true
            }
        }
        val classPath = Paths.get(Application::class.java.getResource("/").toURI())
        Logger.info("Class-Path: $classPath")
        val classPathClasses = findRuntimeNames(classPath, filter)
        val jarClasses = findJarClassNames(filter)
        val classes = ArrayList<Class<*>>(loadClassesByNames(classPathClasses.concat(jarClasses)))
        Logger.info("Class-Count: ${classes.size}")
        Logger.info("Scan-Time: ${escape(start)}ms")
        return classes
    }

}
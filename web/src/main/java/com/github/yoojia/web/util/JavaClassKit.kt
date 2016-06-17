package com.github.yoojia.web.util

import com.github.yoojia.web.supports.Filter
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.*

private val Logger = LoggerFactory.getLogger("JavaClass")

fun findRuntimeNames(based: Path, filter: Filter<String>): List<String> {
    val out = ArrayList<String>()
    Files.walkFileTree(based, object : SimpleFileVisitor<Path>() {
        @Throws(IOException::class)
        override fun visitFile(path: Path, attr: BasicFileAttributes): FileVisitResult {
            val pathName = based.relativize(path).toString()
            if(pathName.endsWith(".class")) {
                val name = resolveClassName(pathName)
                Logger.trace("Found class: $name")
                if(filter.accept(name)) { // return true to accept
                    out.add(name);
                }
            }
            return FileVisitResult.CONTINUE
        }
    })
    return out.toList()
}

fun findJarClassNames(filter: Filter<String>): List<String> {
    return emptyList() // TODO
}

fun loadClassByName(names: List<String>): List<Class<*>> {
    val classLoader = getClassLoader()
    val out = ArrayList<Class<*>>()
    names.forEach { name ->
        out.add(loadClassByName(classLoader, name))
    }
    return out.toList()
}

fun loadClassByName(loader: ClassLoader, name: String): Class<*> {
    val out: Class<*>
    try{
        out = loader.loadClass(name)
    }catch(err: Exception) {
        throw IllegalAccessException("Fail to load: class<$name>, loader: $loader, message: ${err.message}")
    }
    return out
}

@Suppress("UNCHECKED_CAST")
fun <T> newClassInstance(clazz: Class<*>): T {
    return clazz.newInstance() as T
}

fun getClassLoader(): ClassLoader {
    return Thread.currentThread().contextClassLoader
}

private fun resolveClassName(path: String): String {
    val segments = splitToArray(path, File.separatorChar, false)
    val ret = StringBuilder()
    segments.forEach { seg ->
        if (seg.endsWith(".class")) {
            ret.append(seg.substring(0, seg.length - 6)/* 6: .class */)
        } else {
            ret.append(seg).append('.')
        }
    }
    return ret.toString()
}

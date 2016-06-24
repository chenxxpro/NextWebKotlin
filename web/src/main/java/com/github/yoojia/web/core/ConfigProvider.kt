package com.github.yoojia.web.core

import java.nio.file.Path

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.8
 */
interface ConfigProvider {

    fun get(path: Path): Config
}
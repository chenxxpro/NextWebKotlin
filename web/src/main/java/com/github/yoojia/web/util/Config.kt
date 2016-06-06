package com.github.yoojia.web.util

import com.github.yoojia.web.kernel.Config
import org.yaml.snakeyaml.Yaml
import java.io.FileInputStream
import java.nio.file.Path

/**
 * 配置参数数据包装.不使用Triple的是为使得参数意义更新清晰.
 */
data class ConfigParam(val className: String, val priority: Int, val args: Config)

/**
 * 从指定文件中加载配置信息
 * @param path 配置文件路径
 */
@Suppress("UNCHECKED_CAST")
fun loadConfig(path: Path): Config {
    val stream = FileInputStream(path.toFile())
    try{
        val map = Yaml().load(stream)
        if(map == null) {
            return Config(emptyMap())
        }else{
            return Config(map as Map<String, Any>)
        }
    }finally{
        stream.close()
    }
}

/**
 * 解析配置条目
 */
fun parseConfig(config: Config): ConfigParam {
    return ConfigParam(config.getString("class"),
            config.getInt("priority"),
            config.getConfig("args"))
}
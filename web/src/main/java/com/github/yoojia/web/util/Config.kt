package com.github.yoojia.web.util

import com.github.yoojia.web.core.Config
import org.yaml.snakeyaml.Yaml
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

/**
 * 配置参数数据包装.不使用Triple的是为使得参数意义更新清晰.
 */
data class ConfigParam(val className: String, val priority: Int, val args: Config)

/**
 * 从指定文件中加载配置信息。
 * 查看配置状态可以读取两个字段数值：
 * - config-path 配置文件路径；
 * - config-state 配置文件处理状态；
 *
 * @param path 配置文件路径
 * @return 非null Config对象。
 */
@Suppress("UNCHECKED_CAST")
fun loadConfig(path: Path): Config {
    if(! Files.exists(path)) {
        val values: LinkedHashMap<String, Any>
        values = LinkedHashMap<String, Any>(2)
        values.put("config-path", path.toString())
        values.put("config-state", "FILE-NOT-EXISTS")
        return Config(values)
    }else{
        val stream = FileInputStream(path.toFile())
        val values: LinkedHashMap<String, Any>
        try{
            val map = Yaml().load(stream)
            if(map == null) {
                values = LinkedHashMap<String, Any>()
                values.put("config-state", "LOAD-FAIL")
            }else{
                values = map as LinkedHashMap<String, Any>
                values.put("config-state", "LOAD-SUCCESS")
            }
        }finally{
            stream.close()
        }
        values.put("config-path", path.toString())
        return Config(values)
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
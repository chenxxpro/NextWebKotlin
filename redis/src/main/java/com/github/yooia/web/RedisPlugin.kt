package com.github.yooia.web

import com.github.yoojia.web.core.Config
import com.github.yoojia.web.core.Context
import com.github.yoojia.web.core.Plugin
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.Protocol

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.16
 */
class RedisPlugin : Plugin {

    companion object {

        private val Logger = LoggerFactory.getLogger(RedisPlugin::class.java)

        private var REAL_POOL: JedisPool? = null

        internal val CONNECTION: JedisPool by lazy { REAL_POOL!! }

    }

    override fun onCreated(context: Context, config: Config) {
        val host = config.getString("host")
        val password = config.getString("password")
        val port = config.getInt("port", 6379)
        val secret = config.getBoolean("secret", true)
        val log = StringBuilder()
        log.append(", host=${if(secret) "[secret]" else host}")
        log.append(", pass=${if(secret) "[secret]" else password}")
        Logger.debug("Init REDIS plugin: $log")
        val jConfig = JedisPoolConfig()
        jConfig.maxTotal = config.getInt("max-total", JedisPoolConfig.DEFAULT_MAX_TOTAL)
        jConfig.maxIdle = config.getInt("max-idle", JedisPoolConfig.DEFAULT_MAX_IDLE)
        jConfig.minIdle = config.getInt("min-idle", JedisPoolConfig.DEFAULT_MIN_IDLE)
        val timeout = config.getInt("timeout", Protocol.DEFAULT_TIMEOUT)
        REAL_POOL = JedisPool(jConfig, host, port, timeout, password)
    }

    override fun onDestroy() {
        CONNECTION.destroy()
    }
}
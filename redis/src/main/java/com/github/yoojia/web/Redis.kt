package com.github.yoojia.web

import redis.clients.jedis.Jedis

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.16
 */
object Redis {

    /**
     * 获取一个Redis/Jedis资源
     */
    @JvmStatic fun getResource(): Jedis {
        return RedisPlugin.CONNECTION.resource
    }

    @JvmStatic fun <T> auto(action: (redis: Jedis)->T) : T {
        return once(action)
    }

    @JvmStatic fun <T> once(action: (redis: Jedis)->T) : T {
        val redis = RedisPlugin.CONNECTION.resource
        try {
            return action.invoke(redis)
        }finally {
            redis.close()
        }
    }

}
# RedisPlugin

## 配置

在主项目中增加依赖：

```gradle
compile "com.github.yoojia:next-web-redis:2.a.16"
```

如果没有引用Jedis依赖，可以手动增加：

```gradle
compile "redis.clients:jedis:2.8.1"
```

## 作为插件启动

在主项目中的next.yml中的plugins增加以下配置

```yml
plugins:
    # others plugin
    - class "com.github.yoojia.web.RedisPlugin"
      args:
        secret: true
        host: "127.0.0.1"
        port: 6379
        password: "YOUR-PASS"
        max-total: 8
        max-idle: 8
        min-idle: 2
```

注意RedisPlugin要在使用前启动。如果其它插件依赖于它，需要先RedisPlugin启动。

## 使用

#### 获取一个Jedis资源

```kotlin
val jedis = Redis.get()
```

使用完成需要调用close接口。

#### 使用自动关闭的Jedis资源

```kotlin
Resis.auto { jedis->
    // do somethie with jedis instance
}
```
# ActiveJDBCPlugin

## 配置

在主项目中增加依赖：

```gradle
compile "com.github.yoojia:next-web-redis:2.a.16"
```

增加仓库地址：

```gradle
repositories {
    ...
    maven { url 'http://repo.javalite.io' }
}
```

如果没有引用Jedis依赖，可以手动增加：

```gradle
compile "org.javalite:activejdbc:1.4.12-SNAPSHOT"
compile "mysql:mysql-connector-java:5.1.25"
```

## 作为插件启动

在主项目中的next.yml中的plugins增加以下配置

```yml
plugins:
    # others plugin
    - class "com.github.yoojia.web.ActiveJDBCPlugin"
      args:
        secret: true
        driver: "com.mysql.jdbc.Driver"
        uri: "jdbc:mysql://192.168.1.123/testdb?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8"
        user: "root"
        pass: "123456"
```

注意 ActiveJDBCPlugin 要在使用前启动。如果其它插件依赖于数据库，需要先 ActiveJDBCPlugin 启动。

## 使用

#### 使用ActiveJDBC原生的接口

```kotlin
Base.open(...)
```

使用完成需要调用close接口。

#### 使用自动关闭的数据库连接资源

```kotlin
ActiveJDBC.once {
    // do something with database open
}

ActiveJDBC.trans {
    // do something with in database transaction
}
```
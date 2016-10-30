package com.github.yoojia.web

import org.javalite.activejdbc.Base
import org.slf4j.LoggerFactory

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
class ActiveJDBCPlugin : Plugin {

    companion object {

        private val Logger = LoggerFactory.getLogger(ActiveJDBCPlugin::class.java)

        private var DRIVER: String = "NOT-SET-DRIVER"
        private var URI: String = "NOT-SET-URI"
        private var USER: String = "NOT-SET-USER"
        private var PASSWORD: String = "NOT-SET-PASSWORD"

        @JvmStatic fun justOpen(){
            Base.open(ActiveJDBCPlugin.DRIVER,
                    ActiveJDBCPlugin.URI,
                    ActiveJDBCPlugin.USER,
                    ActiveJDBCPlugin.PASSWORD)
        }

        @JvmStatic fun openTransaction(){
            justOpen()
            Base.openTransaction()
        }

        @JvmStatic fun justClose(){
            Base.close()
        }

        @JvmStatic fun closeTransaction(){
            Base.commitTransaction()
            justClose()
        }

    }

    override fun onCreated(context: Context, config: Config) {
        val driver = config.getStringValue("driver")
        val uri = config.getStringValue("uri")
        val username = config.getStringValue("user")
        val password = config.getStringValue("password")
        val secret = config.getBooleanValue("secret")
        val log = StringBuilder("uri=$uri")
        log.append(", user=${if(secret) "[secret]" else username}")
        log.append(", password=${if(secret) "[secret]" else password}")
        Logger.debug("Init database plugin: $log")
        val checkNotSet = fun(field: String): Boolean {
            return field.isNullOrEmpty() || field.startsWith("NOT-SET-")
        }
        if(checkNotSet(driver) || checkNotSet(uri) || checkNotSet(username) || checkNotSet(password)) {
            throw IllegalArgumentException("Database config <driver, uri, user, activejdbc> is required !")
        }
        ActiveJDBCPlugin.DRIVER = driver
        ActiveJDBCPlugin.URI = uri
        ActiveJDBCPlugin.USER = username
        ActiveJDBCPlugin.PASSWORD = password
    }

    override fun onDestroy() {

    }
}
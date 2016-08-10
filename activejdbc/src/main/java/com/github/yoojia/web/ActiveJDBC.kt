package com.github.yoojia.web

import com.github.yoojia.lang.Promise
import org.javalite.activejdbc.Base

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
object ActiveJDBC {

    @JvmStatic fun <T> once(action: ()->T) : T{
        val hasBefore = Base.hasConnection()
        if(!hasBefore) {
            ActiveJDBCPlugin.justOpen()
        }
        try{
            return action.invoke()
        }finally{
            if(!hasBefore) {
                ActiveJDBCPlugin.justClose()
            }
        }
    }

    @JvmStatic fun <T> trans(action: ()->T): T{
        val hasBefore = Base.hasConnection()
        if(!hasBefore) {
            ActiveJDBCPlugin.openTransaction()
        }
        try{
            return action.invoke()
        }finally{
            if(!hasBefore) {
                ActiveJDBCPlugin.closeTransaction()
            }
        }
    }

    @JvmStatic fun <T> promise(action: ()->T): Promise<T> {
        return Promise({ action.invoke() })
    }

}
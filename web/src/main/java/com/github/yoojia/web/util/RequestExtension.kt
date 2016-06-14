package com.github.yoojia.web.util

import com.github.yoojia.web.Request


fun Request.param(key: String, def: String): String {
    val value = param(key)
    if(value.isNullOrEmpty()) {
        return def
    }else{
        return value!!
    }
}

fun Request.param(key: String, def: Int): Int {
    val value = param(key)
    if(value.isNullOrEmpty()) {
        return def
    }else{
        return value!!.toInt()
    }
}

fun Request.param(key: String, def: Long): Long {
    val value = param(key)
    if(value.isNullOrEmpty()) {
        return def
    }else{
        return value!!.toLong()
    }
}

fun Request.param(key: String, def: Float): Float {
    val value = param(key)
    if(value.isNullOrEmpty()) {
        return def
    }else{
        return value!!.toFloat()
    }
}

fun Request.param(key: String, def: Double): Double {
    val value = param(key)
    if(value.isNullOrEmpty()) {
        return def
    }else{
        return value!!.toDouble()
    }
}

fun Request.param(key: String, def: Boolean): Boolean {
    val value = param(key)
    if(value.isNullOrEmpty()) {
        return def
    }else{
        return value!!.toBoolean()
    }
}

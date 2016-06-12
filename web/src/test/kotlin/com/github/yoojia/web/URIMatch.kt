package com.github.yoojia.web

import com.github.yoojia.web.util.isUriResourceMatched
import com.github.yoojia.web.util.splitUri
import org.junit.Assert
import org.junit.Test

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
class URIMatch {

    @Test
    fun test_1(){
        val req = "/"
        val define = "/admin/*"
        Assert.assertFalse(isUriResourceMatched(splitUri(req), splitUri(define)))
    }

    @Test
    fun test_2(){
        val req = "/admin/profile/yoojia"
        val define = "/admin/*"
        Assert.assertTrue(isUriResourceMatched(splitUri(req), splitUri(define)))
    }

    @Test
    fun test_3(){
        val req = "/admin/yoojia/profile"
        val define = "/admin/{username}/*"
        Assert.assertTrue(isUriResourceMatched(splitUri(req), splitUri(define)))
    }
}
package com.github.yoojia.web

import com.github.yoojia.web.util.RequestDefine
import com.github.yoojia.web.util.dynamicParams
import com.github.yoojia.web.util.isRequestMatched
import com.github.yoojia.web.util.splitUri
import org.junit.Assert
import org.junit.Test

/**
 * @author Yoojia Chen (yoojia.chen@gmail.com)
 * @since 1.0
 */
class WebURITest {

    @Test
    fun testSplitURI() {
        val uri = "/users/yoojia"
        val resources = splitUri(uri)
        Assert.assertEquals(3, resources.size)
        Assert.assertEquals("/", resources[0])
        Assert.assertEquals("yoojia", resources[2])
    }

    @Test
    fun testSplitURI_1() {
        val uri = "/users/yoojia/profile"
        val resources = splitUri(uri)
        Assert.assertEquals(4, resources.size)
        Assert.assertEquals("/", resources[0])
        Assert.assertEquals("yoojia", resources[2])
        Assert.assertEquals("profile", resources[3])
    }

    @Test
    fun testIsDynamic() {
        val req = "/users/yoojia/profile"
        val define = "/users/{username}/profile"
        val params = dynamicParams(splitUri(req), RequestDefine(emptyList(), define))
        Assert.assertEquals(true, params.isNotEmpty())
        Assert.assertEquals(true, params.contains("username"))
        Assert.assertEquals("yoojia", params["username"])
    }

    @Test
    fun testIsDynamic_1() {
        val req = "/users/yoojia/profile/10086"
        val define = "/users/{username}/profile/{id}"
        val params = dynamicParams(splitUri(req), RequestDefine(emptyList(), define))
        Assert.assertEquals(true, params.isNotEmpty())
        Assert.assertEquals(true, params.contains("username"))
        Assert.assertEquals(true, params.contains("id"))
        Assert.assertEquals("yoojia", params["username"])
        Assert.assertEquals("10086", params["id"])
    }

    @Test
    fun testURIMatched() {
        val req = "/users/yoojia/profile/10086"
        val define = "/users/{username}/profile/{id}"
        Assert.assertEquals(true, isRequestMatched(RequestDefine(listOf("POST"), req), RequestDefine(listOf("ALL"), define)))
        Assert.assertEquals(true, isRequestMatched(RequestDefine(listOf("GET"), req), RequestDefine(listOf("ALL"), define)))
        Assert.assertEquals(true, isRequestMatched(RequestDefine(listOf("PUT"), req), RequestDefine(listOf("ALL"), define)))
        Assert.assertEquals(true, isRequestMatched(RequestDefine(listOf("DELETE"), req), RequestDefine(listOf("ALL"), define)))
    }

    @Test
    fun testURIMatched0() {
        val req = "/users/yoojia/"
        val define = "/users/{username}"
        Assert.assertEquals(true, isRequestMatched(RequestDefine(listOf("POST"), req), RequestDefine(listOf("POST"), define)))
        Assert.assertEquals(false, isRequestMatched(RequestDefine(listOf("GET"), req), RequestDefine(listOf("POST"), define)))
        Assert.assertEquals(false, isRequestMatched(RequestDefine(listOf("DELETE"), req), RequestDefine(listOf("POST"), define)))
        Assert.assertEquals(false, isRequestMatched(RequestDefine(listOf("PUT"), req), RequestDefine(listOf("POST"), define)))
    }

    @Test
    fun testURIMatched_1() {
        val req = "/users/yoojia/profile/10086"
        val define = "/not-match/{username}/profile/{id}"
        Assert.assertEquals(false, isRequestMatched(RequestDefine(listOf("GET"), req), RequestDefine(listOf("POST", "GET"), define)))
    }

    @Test
    fun testURIMatched_2() {
        val req = "/users/yoojia/profile/10086"
        val define = "/users/{username}/"
        Assert.assertEquals(false, isRequestMatched(RequestDefine(listOf("GET"), req), RequestDefine(listOf("ALL"), define)))
    }

    @Test
    fun testURIMatched_3() {
        val req = "/users/yoojia/profile/10086"
        val define = "/users/{username}/*"
        Assert.assertEquals(true, isRequestMatched(RequestDefine(listOf("GET"), req), RequestDefine(listOf("ALL"), define)))
        Assert.assertEquals(true, isRequestMatched(RequestDefine(listOf("GET"), req), RequestDefine(listOf("GET"), define)))
        Assert.assertEquals(false, isRequestMatched(RequestDefine(listOf("GET"), req), RequestDefine(listOf("POST"), define)))
    }

    @Test
    fun testURIMatched_4() {
        val req = "/"
        val define = "/admin/*"
        Assert.assertEquals(false, isRequestMatched(RequestDefine(listOf("GET"), req), RequestDefine(listOf("GET"), define)))
    }

    @Test
    fun testURIMatched_5() {
        val req = "/admin"
        val define = "/admin/permission/access/*"
        Assert.assertEquals(false, isRequestMatched(RequestDefine(listOf("GET"), req), RequestDefine(listOf("GET"), define)))
    }

}
package com.github.yoojia.web

import com.github.yoojia.web.supports.RequestWrapper
import com.github.yoojia.web.util.splitToArray
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
        val resources = splitToArray(uri)
        Assert.assertEquals(3, resources.size)
        Assert.assertEquals("/", resources[0])
        Assert.assertEquals("yoojia", resources[2])
    }

    @Test
    fun testSplitURI_1() {
        val uri = "/users/yoojia/profile"
        val resources = splitToArray(uri)
        Assert.assertEquals(4, resources.size)
        Assert.assertEquals("/", resources[0])
        Assert.assertEquals("yoojia", resources[2])
        Assert.assertEquals("profile", resources[3])
    }

    @Test
    fun testIsDynamic() {
        val req = "/users/yoojia/profile"
        val define = "/users/{username}/profile"
        val params = RequestWrapper.define("all", define).parseDynamic(splitToArray(req))
        Assert.assertEquals(true, params.isNotEmpty())
        Assert.assertEquals(true, params.contains("username"))
        Assert.assertEquals("yoojia", params["username"])
    }

    @Test
    fun testIsDynamic_1() {
        val req = "/users/yoojia/profile/10086"
        val define = "/users/{username}/profile/{id}"
        val params = RequestWrapper.define("all", define).parseDynamic(splitToArray(req))
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
        Assert.assertEquals(true, RequestWrapper.define("post", req).isRequestMatchDefine(RequestWrapper.define("all", define)))
        Assert.assertEquals(true, RequestWrapper.define("get", req).isRequestMatchDefine(RequestWrapper.define("all", define)))
        Assert.assertEquals(true, RequestWrapper.define("put", req).isRequestMatchDefine(RequestWrapper.define("all", define)))
        Assert.assertEquals(true, RequestWrapper.define("delete", req).isRequestMatchDefine(RequestWrapper.define("all", define)))
    }

    @Test
    fun testURIMatched0() {
        val req = "/users/yoojia/"
        val define = "/users/{username}"
        Assert.assertEquals(true, RequestWrapper.define("post", req).isRequestMatchDefine(RequestWrapper.define("post", define)))
        Assert.assertEquals(false, RequestWrapper.define("get", req).isRequestMatchDefine(RequestWrapper.define("post", define)))
        Assert.assertEquals(false, RequestWrapper.define("delete", req).isRequestMatchDefine(RequestWrapper.define("post", define)))
        Assert.assertEquals(false, RequestWrapper.define("put", req).isRequestMatchDefine(RequestWrapper.define("post", define)))
    }

    @Test
    fun testURIMatched_1() {
        val req = "/users/yoojia/profile/10086"
        val define = "/not-match/{username}/profile/{id}"
        val reqWrap = RequestWrapper.define("get", req)
        val defWrap = RequestWrapper.define("get", define)
        Assert.assertEquals(false, reqWrap.isRequestMatchDefine(defWrap))
    }

    @Test
    fun testURIMatched_2() {
        val req = "/users/yoojia/profile/10086"
        val define = "/users/{username}/"
        Assert.assertEquals(false, RequestWrapper.define("get", req).isRequestMatchDefine(RequestWrapper.define("all", define)))
    }

    @Test
    fun testURIMatched_3() {
        val req = "/users/yoojia/profile/10086"
        val define = "/users/{username}/*"
        Assert.assertEquals(true, RequestWrapper.define("get", req).isRequestMatchDefine(RequestWrapper.define("all", define)))
        Assert.assertEquals(true, RequestWrapper.define("get", req).isRequestMatchDefine(RequestWrapper.define("get", define)))
        Assert.assertEquals(false, RequestWrapper.define("get", req).isRequestMatchDefine(RequestWrapper.define("post", define)))
    }

    @Test
    fun testURIMatched_4() {
        val req = "/"
        val define = "/admin/*"
        Assert.assertEquals(false, RequestWrapper.define("get", req).isRequestMatchDefine(RequestWrapper.define("get", define)))
    }

    @Test
    fun testURIMatched_5() {
        val req = "/admin"
        val define = "/admin/permission/access/*"
        Assert.assertEquals(false, RequestWrapper.define("get", req).isRequestMatchDefine(RequestWrapper.define("get", define)))
    }

}
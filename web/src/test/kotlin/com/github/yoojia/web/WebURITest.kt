package com.github.yoojia.web

import com.github.yoojia.web.supports.RequestWrapper
import com.github.yoojia.web.supports.UriSegment
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
        Assert.assertEquals(true, RequestWrapper.request("post", req, splitToArray(req)).isRequestMatchDefine(RequestWrapper.define("all", define)))
        Assert.assertEquals(true, RequestWrapper.request("get", req, splitToArray(req)).isRequestMatchDefine(RequestWrapper.define("all", define)))
        Assert.assertEquals(true, RequestWrapper.request("put", req, splitToArray(req)).isRequestMatchDefine(RequestWrapper.define("all", define)))
        Assert.assertEquals(true, RequestWrapper.request("delete", req, splitToArray(req)).isRequestMatchDefine(RequestWrapper.define("all", define)))
    }

    @Test
    fun testURIMatched0() {
        val req = "/users/yoojia/"
        val define = "/users/{username}"
        Assert.assertEquals(true, RequestWrapper.request("post", req, splitToArray(req)).isRequestMatchDefine(RequestWrapper.define("post", define)))
        Assert.assertEquals(false, RequestWrapper.request("get", req, splitToArray(req)).isRequestMatchDefine(RequestWrapper.define("post", define)))
        Assert.assertEquals(false, RequestWrapper.request("delete", req, splitToArray(req)).isRequestMatchDefine(RequestWrapper.define("post", define)))
        Assert.assertEquals(false, RequestWrapper.request("put", req, splitToArray(req)).isRequestMatchDefine(RequestWrapper.define("post", define)))
    }

    @Test
    fun testURIMatched_1() {
        val req = "/users/yoojia/profile/10086"
        val define = "/not-match/{username}/profile/{id}"
        val reqWrap = RequestWrapper.request("get", req, splitToArray(req))
        val defWrap = RequestWrapper.define("get", define)
        Assert.assertEquals(false, reqWrap.isRequestMatchDefine(defWrap))
    }

    @Test
    fun testURIMatched_2() {
        val req = "/users/yoojia/profile/10086"
        val define = "/users/{username}/"
        Assert.assertEquals(false, RequestWrapper.request("get", req, splitToArray(req)).isRequestMatchDefine(RequestWrapper.define("all", define)))
    }

    @Test
    fun testURIMatched_3() {
        val req = "/users/yoojia/profile/10086"
        val define = "/users/{username}/*"
        Assert.assertEquals(true, RequestWrapper.request("get", req, splitToArray(req)).isRequestMatchDefine(RequestWrapper.define("all", define)))
        Assert.assertEquals(true, RequestWrapper.request("get", req, splitToArray(req)).isRequestMatchDefine(RequestWrapper.define("get", define)))
        Assert.assertEquals(false, RequestWrapper.request("get", req, splitToArray(req)).isRequestMatchDefine(RequestWrapper.define("post", define)))
    }

    @Test
    fun testURIMatched_4() {
        val req = "/"
        val define = "/admin/*"
        Assert.assertEquals(false, RequestWrapper.request("get", req, splitToArray(req)).isRequestMatchDefine(RequestWrapper.define("get", define)))
    }

    @Test
    fun testURIMatched_5() {
        val req = "/admin"
        val define = "/admin/permission/access/*"
        Assert.assertEquals(false, RequestWrapper.request("get", req, splitToArray(req)).isRequestMatchDefine(RequestWrapper.define("get", define)))
    }

    @Test
    fun testURIMatched_6() {
        val req = "/admin/10086"
        val define = "/admin/{int:user_id}"
        Assert.assertEquals(true, RequestWrapper.request("get", req, splitToArray(req)).isRequestMatchDefine(RequestWrapper.define("get", define)))
    }

    @Test
    fun testURIMatched_7() {
        val req = "/admin/123.456"
        val define = "/admin/{float:user_id}"
        Assert.assertEquals(true, RequestWrapper.request("get", req, splitToArray(req)).isRequestMatchDefine(RequestWrapper.define("get", define)))
    }

    @Test
    fun testURIMatched_8() {
        val req = "/admin/yoojia"
        val define = "/admin/{string:user_id}"
        Assert.assertEquals(true, RequestWrapper.request("get", req, splitToArray(req)).isRequestMatchDefine(RequestWrapper.define("get", define)))
    }

    @Test
    fun testURIMatched_9() {
        val req = "/admin/yoojia"
        val define = "/admin/{user_id}"
        Assert.assertEquals(true, RequestWrapper.request("get", req, splitToArray(req)).isRequestMatchDefine(RequestWrapper.define("get", define)))
    }

    @Test
    fun testURIMatched_10() {
        val req = "/admin/123456"
        val define = "/admin/{user_id}"
        Assert.assertEquals(true, RequestWrapper.request("get", req, splitToArray(req)).isRequestMatchDefine(RequestWrapper.define("get", define)))
    }

    @Test
    fun testURIMatched_11() {
        val req = "/admin/123456.0"
        val define = "/admin/{user_id}"
        Assert.assertEquals(true, RequestWrapper.request("get", req, splitToArray(req)).isRequestMatchDefine(RequestWrapper.define("get", define)))
    }

    @Test
    fun testURIMatched_12() {
        val define = "/admin/{int:user_id}"
        val req1 = "/admin/123.0"
        val req2 = "/admin/abc"
        Assert.assertEquals(false, RequestWrapper.request("get", req1, splitToArray(req1)).isRequestMatchDefine(RequestWrapper.define("get", define)))
        Assert.assertEquals(false, RequestWrapper.request("get", req2, splitToArray(req2)).isRequestMatchDefine(RequestWrapper.define("get", define)))
    }

    @Test
    fun testURIMatched_13() {
        val define = "/admin/{float:user_id}"
        val req1 = "/admin/123"
        val req2 = "/admin/abc"
        Assert.assertEquals(false, RequestWrapper.request("get", req1, splitToArray(req1)).isRequestMatchDefine(RequestWrapper.define("get", define)))
        Assert.assertEquals(false, RequestWrapper.request("get", req2, splitToArray(req2)).isRequestMatchDefine(RequestWrapper.define("get", define)))
    }

    @Test
    fun testURIMatched_14() {
        val define = "/admin/{string:user_id}"
        val req1 = "/admin/123"
        val req2 = "/admin/123.456"
        Assert.assertEquals(false, RequestWrapper.request("get", req1, splitToArray(req1)).isRequestMatchDefine(RequestWrapper.define("get", define)))
        Assert.assertEquals(false, RequestWrapper.request("get", req2, splitToArray(req2)).isRequestMatchDefine(RequestWrapper.define("get", define)))
    }

}
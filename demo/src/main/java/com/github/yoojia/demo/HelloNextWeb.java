package com.github.yoojia.demo;

import com.github.yoojia.web.Request;
import com.github.yoojia.web.RequestChain;
import com.github.yoojia.web.Response;
import com.github.yoojia.web.http.Controller;
import com.github.yoojia.web.supports.GET;
import com.github.yoojia.web.supports.POST;

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
@Controller("/admin")
public class HelloNextWeb {

    @GET("/hello/{username}")
    public void hello(Request request, Response response, RequestChain chain) {
        response.sendText("Hello /hello/{username}, username=" + request.param("username"));
        chain.interrupt();
    }

    @GET("/{username}")
    public void dynamic(Request request, Response response) {
        response.sendText("Hello /{username} , username=" + request.param("username"));
        response.sendText(null);
    }

    @GET("/*")
    @POST("/*")
    public void wildcards(Request request, Response response) {
        response.sendText("Hello /* , " + request.param("username"));
    }

    @GET("/yoojia")
    public void statix(Request request, Response response) {
        response.sendText("Hello /yoojia, username=" + request.param("username"));
    }
}

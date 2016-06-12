package com.github.yoojia.demo;

import com.github.yoojia.web.Request;
import com.github.yoojia.web.RequestChain;
import com.github.yoojia.web.Response;
import com.github.yoojia.web.http.Controller;
import com.github.yoojia.web.supports.CachedObjectListener;
import com.github.yoojia.web.supports.GET;
import com.github.yoojia.web.supports.ModuleRequestsListener;
import com.github.yoojia.web.supports.POST;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
@Controller("/admin")
public class HelloNextWeb implements CachedObjectListener, ModuleRequestsListener{

    @GET("/hello/{username}")
    public void hello(Request request, Response response, RequestChain chain) {
        response.sendText("Hello /hello/{username}, username=" + request.dynamicParam("username"));
        chain.interrupt();
    }

    @GET("/{username}")
    public void dynamic(Request request, Response response) {
        response.sendText("Hello /{username} , username=" + request.dynamicParam("username"));
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

    @Override
    public void onCreated() {
        System.out.println("####### Module cached created");
    }

    @Override
    public void onDestroy() {
        System.out.println("####### Module cached destroy");
    }

    @Override
    public void beforeEach(@NotNull Method method, @NotNull Request request, @NotNull Response response) {
        System.out.println("---- Method invoke before: " + method);
    }

    @Override
    public void afterEach(@NotNull Method method, @NotNull Request request, @NotNull Response response) {
        System.out.println("---- Method invoke after: " + method);
    }
}

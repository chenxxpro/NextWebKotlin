package com.github.yoojia.demo.modules;

import com.github.yoojia.web.Request;
import com.github.yoojia.web.RequestChain;
import com.github.yoojia.web.Response;
import com.github.yoojia.web.http.Controller;
import com.github.yoojia.web.supports.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
@Controller("/admin")
public class HelloNextWeb implements ModuleCachedListener, ModuleRequestsListener{

    @GET("/hello/{username}")
    public void hello(Request request, Response response, RequestChain chain) {
        response.sendHtml("<br/>Handle by GET/hello/{username}, username= " + request.dynamicParam("username"));
        chain.interrupt();
    }

    @GET("/{user_id}")
    public void undefined(Request request, Response response, RequestChain chain) {
        response.sendHtml("<br/>Handle by GET/{user_id}, user_id= " + request.dynamicParam("user_id"));
        chain.interrupt();
    }

    @GET("/{string:username}")
    public void dynamicTyped(Request request, Response response) {
        response.sendHtml("<br/>Handle by GET/{string:username} , username= " + request.dynamicParam("username"));
    }

    @GET("/{int:user_id}")
    public void intDynamic(Request request, Response response) {
        response.sendHtml("<br/>Handle by GET/{int:user_id} , user_id= " + request.dynamicParam("user_id"));
    }

    @GETPOST("/*")
    public void getpostWildcards(Request request, Response response) {
        response.sendHtml("<br/>Handle by GET/* , username= " + request.paramOrNull("username"));
    }

    @POST("/yoojia")
    public void statix(Request request, Response response) {
        response.sendHtml("<br/>Handle by POST/yoojia, username= " + request.paramOrNull("username"));
    }

    @PUT("/yoojia")
    public void put(Request request, Response response) {
        response.sendHtml("<br/>Handle by PUT/yoojia, name= " + request.paramOrNull("name") + ", body.data = " + request.bodyData());
        response.sendHtml("<br/>Body.data = " + request.bodyData());
    }

    @DELETE("/yoojia")
    public void delete(Request request, Response response) {
        response.sendHtml("<br/>Handle by DELETE/yoojia, name= " + request.paramOrNull("name") + ", body.data = " + request.bodyData());
        response.sendHtml("<br/>Body.data = " + request.bodyData());
    }

    @Override
    public void onCached() {
        System.out.println("####### Module cached created");
    }

    @Override
    public void onRemoved() {
        System.out.println("####### Module cached destroy");
    }

    @Override
    public void beforeRequests(@NotNull Method method, @NotNull Request request, @NotNull Response response) {
        System.out.println("---- Method invoke before: " + method);
    }

    @Override
    public void afterRequests(@NotNull Method method, @NotNull Request request, @NotNull Response response) {
        System.out.println("---- Method invoke after: " + method);
    }
}

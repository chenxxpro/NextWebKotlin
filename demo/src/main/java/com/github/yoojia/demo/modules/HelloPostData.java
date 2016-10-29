package com.github.yoojia.demo.modules;

import com.github.yoojia.web.Request;
import com.github.yoojia.web.Response;
import com.github.yoojia.web.http.Controller;
import com.github.yoojia.web.supports.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
@Controller("/data")
public class HelloPostData implements ModuleRequestsListener {

    @GET("/post")
    public void get(Request request, Response response){
    }

    @POST("/post")
    public void post(Request request, Response response){

    }

    @DELETE("/post")
    public void delete(Request request, Response response){
    }

    @PUT("/post")
    public void put(Request request, Response response){
    }

    @Override
    public void beforeRequests(@NotNull Method method, @NotNull Request request, @NotNull Response response) {
        response.sendText(request.body());
    }

    @Override
    public void afterRequests(@NotNull Method method, @NotNull Request request, @NotNull Response response) {

    }
}

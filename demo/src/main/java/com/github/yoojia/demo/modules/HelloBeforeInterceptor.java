package com.github.yoojia.demo.modules;

import com.github.yoojia.web.Request;
import com.github.yoojia.web.RequestChain;
import com.github.yoojia.web.Response;
import com.github.yoojia.web.interceptor.BeforeInterceptor;
import com.github.yoojia.web.interceptor.Ignore;
import com.github.yoojia.web.supports.GET;

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
@BeforeInterceptor
public class HelloBeforeInterceptor {

    @GET("/*")
    @Ignore("/templates/*")
    public void used(Request request, Response response){
        response.html("<br/> BeforeInterceptor processed");
    }
}

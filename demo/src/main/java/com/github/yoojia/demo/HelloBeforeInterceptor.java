package com.github.yoojia.demo;

import com.github.yoojia.web.Request;
import com.github.yoojia.web.RequestChain;
import com.github.yoojia.web.Response;
import com.github.yoojia.web.interceptor.BeforeInterceptor;
import com.github.yoojia.web.supports.Route;

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
@BeforeInterceptor
public class HelloBeforeInterceptor {

    @Route(path = "/*")
    public void used(Request request, Response response, RequestChain chain){
//        response.sendText("Before interceptor");
//        chain.stop();
    }
}

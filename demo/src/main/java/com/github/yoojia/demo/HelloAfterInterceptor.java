package com.github.yoojia.demo;

import com.github.yoojia.web.Request;
import com.github.yoojia.web.RequestChain;
import com.github.yoojia.web.Response;
import com.github.yoojia.web.interceptor.AfterInterceptor;
import com.github.yoojia.web.supports.Route;

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
@AfterInterceptor
public class HelloAfterInterceptor {

    @Route(path = "/*")
    public void used(Request request, Response response, RequestChain chain){
//        response.sendText("After interceptor");
//        chain.stop();
    }
}

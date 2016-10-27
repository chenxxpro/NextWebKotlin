package com.github.yoojia.demo.modules;

import com.github.yoojia.web.Request;
import com.github.yoojia.web.Response;
import com.github.yoojia.web.http.Controller;
import com.github.yoojia.web.supports.GET;

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
@Controller("/templates")
public class HelloTemplate {

    @GET("/{username}")
    public void dynamic(Request request, Response response) {
        String username = request.dynamic("username");
        request.putParam("username", username);
        response.template("hello.html");
    }

}

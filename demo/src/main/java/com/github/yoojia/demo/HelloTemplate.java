package com.github.yoojia.demo;

import com.github.yoojia.web.Request;
import com.github.yoojia.web.Response;
import com.github.yoojia.web.http.Module;
import com.github.yoojia.web.supports.Route;

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
@Module(base = "/templates")
public class HelloTemplate {

    @Route(path = "/{username}", methods={"get", "post"})
    public void dynamic(Request request, Response response) {
        String username = request.dynamicParam("username");
        request.putParam("username", username);
        response.template("hello.html");
    }

}

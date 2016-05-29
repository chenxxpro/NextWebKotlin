package com.github.yoojia.demo;

import com.github.yoojia.web.Request;
import com.github.yoojia.web.RequestChain;
import com.github.yoojia.web.Response;
import com.github.yoojia.web.http.Module;
import com.github.yoojia.web.supports.Route;

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
@Module(base = "/admin")
public class HelloNextWeb {

    @Route(path = "/hello/{username}", methods={"get", "post"})
    public void hello(Request request, Response response, RequestChain chain) {
        response.sendText("Hello /hello/{username}, username=" + request.param("username"));
        chain.interrupt();
    }

    @Route(path = "/{username}", methods={"get", "post"})
    public void dynamic(Request request, Response response) {
        response.sendText("Hello /{username} , username=" + request.param("username"));
        response.sendText(null);
    }

    @Route(path = "/*", methods={"get", "post"})
    public void wildcards(Request request, Response response) {
        response.sendText("Hello /* , " + request.param("username"));
    }

    @Route(path = "/yoojia", methods={"get", "post"})
    public void statix(Request request, Response response) {
        response.sendText("Hello /yoojia, username=" + request.param("username"));
    }
}

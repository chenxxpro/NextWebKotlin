package com.github.yoojia.demo;

import com.github.yoojia.web.ProvidedBootstrapServlet;
import com.github.yoojia.web.core.Context;
import com.github.yoojia.web.server.EmbeddedServer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
public class EmbeddedBootstrapServlet extends ProvidedBootstrapServlet{

    @NotNull
    @Override
    public List<Class<?>> get(@NotNull Context context) {
        return from(HelloAfterInterceptor.class,
                HelloBeforeInterceptor.class,
                HelloNextWeb.class,
                HelloPlugin.class,
                HelloTemplate.class);
    }

    public static void main(String[] args) throws Exception {
        final EmbeddedServer server = new EmbeddedServer(8082);
        System.out.println("Base url = " + server.getBaseUrl());
        server.setBootstrapServlet(EmbeddedBootstrapServlet.class);
        server.start();
        server.join();
        server.stop();
    }

}

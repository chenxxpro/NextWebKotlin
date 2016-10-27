package com.github.yoojia.demo.supports;

import com.github.yoojia.demo.modules.HelloAfterInterceptor;
import com.github.yoojia.demo.modules.HelloBeforeInterceptor;
import com.github.yoojia.demo.modules.HelloNextWeb;
import com.github.yoojia.demo.modules.HelloTemplate;
import com.github.yoojia.web.Context;
import com.github.yoojia.web.ProvidedServlet;
import com.github.yoojia.web.server.EmbeddedServer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
public class EmbeddedBootstrapServlet extends ProvidedServlet {

    @NotNull
    @Override
    public List<Class<?>> getClasses(@NotNull Context context) {
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

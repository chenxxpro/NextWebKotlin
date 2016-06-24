package com.github.yoojia.demo;

import com.github.yoojia.web.ProvidedBootstrapServlet;
import com.github.yoojia.web.core.Config;
import com.github.yoojia.web.core.Context;
import com.github.yoojia.web.server.EmbeddedServer;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
public class EmbeddedBootstrapServlet extends ProvidedBootstrapServlet{

    @NotNull
    @Override
    public List<Class<?>> get(@NotNull Context context) {
        final List<Class<?>> classes = new ArrayList<>();
        classes.add(HelloAfterInterceptor.class);
        classes.add(HelloBeforeInterceptor.class);
        classes.add(HelloNextWeb.class);
        classes.add(HelloPlugin.class);
        classes.add(HelloTemplate.class);
        return classes;
    }

    public static void main(String[] args) throws Exception {
        final EmbeddedServer server = new EmbeddedServer(8082);
        server.setBootstrapServlet(EmbeddedBootstrapServlet.class);
        server.start();
        server.join();
        server.stop();
    }

    @NotNull
    @Override
    public Config get(@NotNull Path path) {
        return Config.empty();
    }
}

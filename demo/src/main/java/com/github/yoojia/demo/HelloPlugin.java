package com.github.yoojia.demo;

import com.github.yoojia.web.core.Config;
import com.github.yoojia.web.core.Context;
import com.github.yoojia.web.core.Plugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
public class HelloPlugin implements Plugin {

    private static final Logger logger = LoggerFactory.getLogger(HelloPlugin.class);

    @Override
    public void onCreated(@NotNull Context context, @NotNull Config config) {
        logger.debug("Hello plugin: " + config);
        logger.debug("Hello plugin: " + config.getBoolean("secret"));
        logger.debug("Hello plugin: " + config.getBoolean("secret1"));
    }

    @Override
    public void onDestroy() {

    }
}

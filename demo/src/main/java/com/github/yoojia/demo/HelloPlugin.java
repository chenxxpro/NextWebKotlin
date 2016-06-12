package com.github.yoojia.demo;

import com.github.yoojia.web.core.Config;
import com.github.yoojia.web.core.Context;
import com.github.yoojia.web.core.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
public class HelloPlugin implements Plugin {

    @Override
    public void onCreated(@NotNull Context context, @NotNull Config config) {
        Logger.d("Hello plugin: " + config);
        Logger.d("Hello plugin: " + config.getBoolean("secret"));
        Logger.d("Hello plugin: " + config.getBoolean("secret1"));
    }

    @Override
    public void onDestroy() {

    }
}

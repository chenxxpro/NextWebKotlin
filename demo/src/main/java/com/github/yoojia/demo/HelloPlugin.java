package com.github.yoojia.demo;

import com.github.yoojia.web.kernel.Config;
import com.github.yoojia.web.kernel.Context;
import com.github.yoojia.web.kernel.Plugin;
import com.github.yoojia.web.supports.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
public class HelloPlugin implements Plugin {

    @Override
    public void onCreated(@NotNull Context context, @NotNull Config config) {
        Logger.Companion.d("Hello plugin: " + config);
    }

    @Override
    public void onDestroy() {

    }
}

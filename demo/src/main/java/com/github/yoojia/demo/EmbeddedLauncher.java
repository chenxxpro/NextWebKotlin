package com.github.yoojia.demo;

import com.github.yoojia.web.server.EmbeddedServer;

/**
 * @author YOOJIA.CHEN (yoojia.chen@gmail.com)
 */
public class EmbeddedLauncher {

    public static void main(String[] args) throws Exception {
        EmbeddedServer server = new EmbeddedServer("src/main/webapp", 8080);
        server.run();
    }
}

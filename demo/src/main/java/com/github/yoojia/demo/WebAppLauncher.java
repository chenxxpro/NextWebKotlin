package com.github.yoojia.demo;

import com.github.yoojia.web.server.WebAppServer;

/**
 * @author YOOJIA.CHEN (yoojia.chen@gmail.com)
 */
public class WebAppLauncher {

    public static void main(String[] args) throws Exception {
        WebAppServer server = new WebAppServer(8082);
        server.run("src/main/webapp");
    }
}

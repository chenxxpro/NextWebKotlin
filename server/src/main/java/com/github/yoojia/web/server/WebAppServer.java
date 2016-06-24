package com.github.yoojia.web.server;

import org.eclipse.jetty.webapp.WebAppContext;

/**
 * @author YOOJIA.CHEN (yoojia.chen@gmail.com)
 * @since 2.a.2
 */
public class WebAppServer extends JettyServer{

    public WebAppServer(String host, int port) {
        super(host, port);
    }

    public WebAppServer(int port) {
        super(port);
    }

    public void run(String webApp, String path) throws Exception {
        getServer().setHandler(new WebAppContext(webApp, path));
        start();
        join();
        stop();
    }

    public void run(String webApp) throws Exception{
        run(webApp, "/");
    }

}
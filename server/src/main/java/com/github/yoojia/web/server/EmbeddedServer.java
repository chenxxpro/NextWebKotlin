package com.github.yoojia.web.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * @author YOOJIA.CHEN (yoojia.chen@gmail.com)
 */
public class EmbeddedServer {

    private final String webApp;
    private final int port;

    public EmbeddedServer(String webApp, int port) {
        this.webApp = webApp;
        this.port = port;
    }

    public void run() throws Exception {
        run("/");
    }

    /**
     * Run server
     * @throws Exception
     */
    public void run(String contextPath) throws Exception {
        final Server server = new Server(port);
        server.setHandler(new WebAppContext(webApp, contextPath));
        System.out.println("Embedded-Server-Port: " + port);
        server.start();
        server.join();
        server.stop();
    }

}

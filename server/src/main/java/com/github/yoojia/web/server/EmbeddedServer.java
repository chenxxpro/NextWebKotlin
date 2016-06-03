package com.github.yoojia.web.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * @author YOOJIA.CHEN (yoojia.chen@gmail.com)
 */
public class EmbeddedServer {

    private final String webApp;
    private final int port;
    private final Server server;

    public EmbeddedServer(String webApp, int port) {
        this.server = new Server(port);
        this.webApp = webApp;
        this.port = port;
    }

    public void runAsWebApp() throws Exception {
        runAsWebApp("/");
    }

    /**
     * Run as web app server
     * @throws Exception
     */
    public void runAsWebApp(String contextPath) throws Exception {
        checkRunning();
        server.setHandler(new WebAppContext(webApp, contextPath));
        System.out.println("Embedded-Server run as WebApp on port: " + port);
        server.start();
        server.join();
        server.stop();
    }

    public void start() throws Exception {
        checkRunning();
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    private void checkRunning(){
        if (server.isRunning()) {
            throw new IllegalStateException("Server is running !");
        }
    }

}
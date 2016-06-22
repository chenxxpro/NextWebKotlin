package com.github.yoojia.web.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import javax.servlet.Servlet;
import java.net.InetSocketAddress;

/**
 * @author YOOJIA.CHEN (yoojia.chen@gmail.com)
 */
public class EmbeddedServer {

    private final String webApp;
    private final Server server;
    private final InetSocketAddress address;

    public EmbeddedServer(String webApp, int port) {
        this.address = InetSocketAddress.createUnresolved("localhost", port);
        this.server = new Server(address);
        this.webApp = webApp;
    }

    public EmbeddedServer(int port) {
        this(".", port);
    }

    public void runAsWebApp() throws Exception {
        runAsWebApp("/");
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    /**
     * Run as web app server
     * @throws Exception
     */
    public void runAsWebApp(String contextPath) throws Exception {
        checkRunning();
        server.setHandler(new WebAppContext(webApp, contextPath));
        System.out.println("Embedded-Server RunAsWebApp on: " + address);
        server.start();
        server.join();
        server.stop();
    }

    public void setBootstrapServlet(Class<? extends Servlet> servletClass){
        checkRunning();
        final WebAppContext context = new WebAppContext(webApp, "/");
        context.addServlet(servletClass, "/*").setInitOrder(0);
        server.setHandler(context);
    }

    public void start() throws Exception {
        checkRunning();
        System.out.println("Embedded-Server START on: " + address);
        server.start();
    }

    public void join() throws Exception{
        server.join();
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
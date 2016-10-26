package com.github.yoojia.web.server;

import org.eclipse.jetty.webapp.WebAppContext;

import javax.servlet.Servlet;

/**
 * @author YOOJIA.CHEN (yoojia.chen@gmail.com)
 */
public class EmbeddedServer extends JettyServer {

    private boolean mSetBootstrap = false;

    public EmbeddedServer(int port) {
        super("0.0.0.0", port);
    }

    public void setBootstrapServlet(Class<? extends Servlet> servletClass){
        mSetBootstrap = true;
        final WebAppContext context = new WebAppContext(".", "/");
        context.addServlet(servletClass, "/*").setInitOrder(0);
        getServer().setHandler(context);
    }

    @Override
    public void start() throws Exception {
        if (!mSetBootstrap) {
            throw new IllegalStateException("BootstrapServlet not set! Call EmbeddedServer.setBootstrapServlet() to set");
        }
        super.start();
    }
}
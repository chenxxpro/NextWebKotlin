package com.github.yoojia.web.server;

import org.eclipse.jetty.server.Server;

import java.net.InetSocketAddress;

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.2
 */
public class JettyServer {

    private final Server mServer;
    private final InetSocketAddress mAddress;

    public JettyServer(String host, int port) {
        this.mAddress = InetSocketAddress.createUnresolved(host, port);
        this.mServer = new Server(mAddress);
    }

    public JettyServer(int port) {
        this("localhost", port);
    }

    public InetSocketAddress getAddress() {
        return mAddress;
    }

    public void start() throws Exception {
        checkRunning();
        System.out.println("Server START on: " + mAddress);
        mServer.start();
    }

    public void join() throws Exception{
        mServer.join();
    }

    public void stop() throws Exception {
        mServer.stop();
    }

    public Server getServer() {
        return mServer;
    }

    private void checkRunning(){
        if (mServer.isRunning()) {
            throw new IllegalStateException("Server is running !");
        }
    }
}

package com.github.yoojia.web.server;

import org.eclipse.jetty.server.Server;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;

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

    public URL getBaseUrl(boolean ssl){
        try {
            return new URL("http" + (ssl?"s":"") + "://" + mAddress.toString());
        } catch (MalformedURLException e) {
            throw new RuntimeException("IT CAN'T BE");
        }
    }

    public String getBaseUrl() {
        return getBaseUrl(false).toString();
    }

    public String getSSLBaseUrl(){
        return getBaseUrl(true).toString();
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

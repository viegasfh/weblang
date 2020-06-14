package weblx.webserver;

import java.io.*;
import java.net.*;
import java.util.*;

import webl.lang.expr.*;

public class WebServer extends Thread
{
    private static boolean      running = false;
    private static int          port;
    static String               root;
    private static WebServer    svr;
    
    private static ServerSocket S;
    
    private static Hashtable    funs = new Hashtable();
    
    static synchronized public void Start(String root, int port) throws IOException {
        if (running) return;
        WebServer.port = port;
        WebServer.root = root;
        svr = new WebServer();
    }
    
    static synchronized public void Stop() throws IOException {
        if (!running) return;
        S.close();
    }
    
    private WebServer() throws IOException {
        S = new ServerSocket(port, 100);
        start();
    }
    
    public void run() {
        running = true;
        try {
            while (true) {
                Socket socket = S.accept();
                new Connection(socket);
            }
        } catch(IOException e) {
        } finally {
            running = false;
        }
    }
    
    static public synchronized void Publish(String name, FunExpr fun) {
        funs.put(name, fun);
    }
    
    static public synchronized FunExpr GetFun(String name) {
        Object o = funs.get(name);
        if (o != null)
            return (FunExpr)o;
        else
            return null;
    }
}

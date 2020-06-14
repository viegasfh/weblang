package weblx.webserver;

import java.io.*;
import java.net.*;
import java.util.*;

import webl.lang.*;
import webl.lang.expr.*;
import webl.util.*;

public class Connection extends Thread
{
    private Socket          socket;
    private BufferedReader  in;
    private DataOutputStream    out;
    
    public Connection(Socket socket) {
        this.socket = socket;
        start();
    }
    
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            
            Request req = GetRequest();
            Response res = GetResponse(req);
            
            res.write(out);
            out.flush();
            socket.close();
        } catch(IOException e) {
        }
    }
    
    private Request GetRequest() {
        String line;
        Request req = new Request();
        
        try {
            line = in.readLine();
            if(line == null) 
                return null;
            
            // read the first request line
            StringTokenizer T = new StringTokenizer(line);
            if (T.hasMoreTokens()) {
                req.setMethod(T.nextToken());
                if (T.hasMoreTokens()) {
                    req.setURI(T.nextToken());
                    if (T.hasMoreTokens()) {
                        req.setProtocol(T.nextToken().toUpperCase());
                    }
                } else 
                    return null;
            } else 
                return null;
            
            String proto = req.getProtocol();
            if (proto.equals("HTTP/1.0") || proto.equals("HTTP/1.1")) {
                while(true) {
                    line = in.readLine();
                    if (line == null || line.equals(""))
                        break;
                    
                    int pos = line.indexOf(": ");
                    if (pos != -1) {
                        String name = line.substring(0, pos);
                        String val = line.substring(pos + 2);
                        req.setHeader(name, val);
                    }
                }
            } else if (proto.equals("HTTP/0.9")) {
            } else     // unknown protocol
                return null;
            
            StringBuffer res = new StringBuffer();
            while (in.ready()) {
                line = in.readLine();
                res.append(line + "\r\n");
            }
            req.setContents(res.toString());
            Log.debugln("[" + req.getProtocol() + " " + req.getMethod() + " " + req.getURI() + "]");
            return req;
        } catch (IOException e) {
            return null;
        }
    }    
    
    Response GetResponse(Request req) {
        if (req == null)
            return new Response(400, "Bad request");
            
        Response res = null;
        String path = req.getPath();
        
        res = Execute(path, req);
        if (res == null) {        // Execute unsuccessful, so check if the file exists
            String fname = path.replace('/', File.separatorChar);
            if (fname.startsWith(File.separator)) {
                fname = fname.substring(1);
            }
            
            File file = new File(WebServer.root, fname);
            String abs;
            try {
                abs = file.getCanonicalPath();
            } catch (IOException e) {
                res = new Response(404, "File not found");
                Log.debugln("[Server: File not found/Access denied " + file.getAbsolutePath() + "]");
                return res;
            }
            
            // check if file is outside of the web server root
            if (!abs.startsWith(WebServer.root)) {
                res = new Response(400, "Access denied");
                Log.debugln("[Server: Access denied " + abs + "; prefix does not match server root " +
                    WebServer.root + "]");
                return res;
            }
            
            Log.debugln("[Server: File " + abs + "]");
            if (file.isDirectory()) {
                File ind = new File(file, "index.html");
                if (ind.exists()) {
                    res = new Response(200, "OK");
                    res.setResult(ind);
                } else {
                    res = new Response(404, "File not found");
                    Log.debugln("[Server: File not found/Access denied " + abs + "]");
                }
            } else if (file.exists()) {
                res = new Response(200, "OK");
                res.setResult(file);
            } else {
                res = new Response(404, "File not found");
                Log.debugln("[Server: File not found/Access denied " + abs + "]");
            }
        }
        return res;
    }
    
    protected Response Execute(String path, Request req) {
        FunExpr f = WebServer.GetFun(path);
        if (f == null) 
            return null;
        
        Response res = new Response(200, "OK");
        
        if (f.args.size() != 2) {
            res.setHeader("Content-Type", "text/html");
            res.def("result",
                Program.Str("Server Error: the WebL function invoked by the web server does not have the correct signature"));
            res.setStatus(400, "Bad request");
            return res;
        }

        Context newcontext = new Context(f.c, null, f.scope, Program.Str("webserver"));
        newcontext.binding[0] = req;
        newcontext.binding[1] = res;
        try {
            f.body.eval(newcontext);
            res.setHeader("Last-Modified", new Date().toString());  // hopefully prevents caching
            return res;
        } catch (WebLException e) {
            res.setHeader("Content-Type", "text/html");
            res.def("result", Program.Str("<h1>A WebL exception occured while executing " + path + "</h1><pre>" + e.report() + "</pre>"));
            res.setStatus(400, "Bad request");
            return res;
        }
    }    
}

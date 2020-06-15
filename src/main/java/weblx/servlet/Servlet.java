package weblx.servlet;

import webl.util.*;
import webl.lang.*;
import webl.lang.expr.*;
import webl.util.*;

import java.util.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

/*

The class weblx.servlet.Servlet implements a Java servlet. By plassing this class (or the jar file
it is in) on a Servlet enabled web-server, it becomes possible to execute WebL code directly on
the server. Web surfers may access your servlet WebL code by URL of typically the following form:

    http://www.host.com/servlet/weblx.servlet.Servlet/modulename_variablename?arguments
    
In case your web server supports aliases, you can alias "weblx.servlet.Servlet" as "webl", which
allows access from the following URL:

    http://www.host.com/servlet/webl/modulename_variablename?arguments

In both cases, "modulename" identifies the WebL module that contains the script, and "variablename" 
identifies an exported variable in that module. The value type of this variable must be a function
with two  formal arguments. The first time the URL is accessed, the module will be loaded 
automatically (this happens ONLY once; afterwards the module is cached).

Table I and II below show the format of the two arguments of the function. The first is the request 
object, and the second is the response object. The explanation for the field values is found in the
Java servlet specification available from Javasoft.

SERVER SETUP

Servlet setup can be complicated. First make sure that you can access the demo servlets that come
with your web server. THEN continue with this checklist:

1. Put WebL.jar is the CLASSPATH of your web server (server dependent).

2. Add a configuration parameter for the WebL servlet:

    Parameter name: webl.path
    Parameter value: (search path for WebL scripts)
    
3. Restart your web server for changes to take affect.

4. Place WebL scripts in the search path (one of the scripts below should do).


SERVLET DEVELOPMENT

You may modify your WebL servlets while being used. WebL checks before each browser access whether the 
WebL module has changed or not (file lastmodified date). If the modified date is different from the
modified date when the module was loaded first, the module is auotmatically reloaded.


Example 1. SETTING AND RETRIEVING THE VALUE OF A VARIABLE:

    // File: Example1.webl
    
    var theval = nil;

    // http://www.host.com/servlet/webl/Example1_SetVal?x=hello
    export var
    	SetVal = fun(req, res)
    		theval = req.param.x;
    		res.mimetype = "text/plain";
    		res.result = "Set val to " + theval;
    	end;
    
    // http://www.host.com/servlet/webl/Example1_GetVal
    export var
    	GetVal = fun(req, res)
    		res.mimetype = "text/plain";
    		res.result = "Val is " + theval;
    	end;
    	
    	
Example 2. SNOOPING REQUEST HEADERS

    // File: Example2.webl
    
    var Decode = fun(req)
    	var s = "Header snoop:\n\n";
    	every field in req do
    	  s = s + ToString(field) + ": " + ToString(req[field]) + "\n";
    	end;
    	s;
    end;

    // http://www.host.com/servlet/webl/Example2_Snoop
    export var
    	Snoop = fun(req, res)
    		res.mimetype = "text/plain";
    		res.result = Decode(req);
    	end;    	


Example 3. USING COOKIES TO IMPLEMENT A COUNTER

    // File: Example3.webl
    
    // http://www.host.com/servlet/webl/Example3_Count
    export var
    	Count = fun(req, res)
    	    res.result = "Cookie test\n";

    		var count =
    			ToInt(req.cookies.cc) ?
    			begin
         		  res.result = res.result + "No cookie so far\n";
    			  0
    			end;

    		res.result = res.result + "Cookie Count = " + count;
    		res.mimetype = "text/plain";
    		
    		// set new cookie
    		res.cookies = [.
    			cc = [. domain="www.myhost.com", path="/", value=count+1,
        			comment="", maxage=-1, version=0 .]
    		.]
    	end;
	
Table I. Format of the request object:

    [.
        method: string,
        requestURI: string,
        servletpath: string,
        pathinfo: string,
        pathtranslated: string,
        querystring: string,
        remoteuser: string,
        authtype: string,
        
        remoteaddr: string,
        remotehost: string,
        scheme: string,
        servername: string,
        serverport: string,
        protocol: string,
        contenttype: string,
        
        header: [. string : string .],
        param: [. string : (string |list) .],            // list in case of multiple parameters with the same name
        cookies : [. string : string .]
        
    .]
    
Table II. Format of the response object:
    
    [.
        statuscode: int,
        statusmsg: string,
        result: string,
        mimetype: string
        header: [. string : string .],
        cookies : [. string : 
                [. 
                    comment: string, 
                    domain: string, 
                    maxage: int, 
                    path: string, 
                    secure: bool, 
                    value: string, 
                    version: int
                .] 
            .]
    .]
*/

public class Servlet extends HttpServlet
{
    Machine machine;
    
    public void init(ServletConfig config)	throws ServletException {
	    super.init(config);
	    
        String[] args = new String[1];
        int arg0 = 0;
        
        // set up the output log where errors during startup are reported
        Logger startuplog = new StringLog(true);
        
        // set up the default output logs, goes to the console
        Log.SetLogger(new ConsoleLog(false));        
        
        try {
            String dirs = config.getInitParameter("webl.path");
            if (dirs != null)
                FileLocator.AddSearchDirs(dirs);
            
            // load properties
            Properties props = new Properties(System.getProperties());
    		InputStream in = FileLocator.Find("webl.properties");
    		if (in != null) {
    		    props.load(new BufferedInputStream(in));
    		    System.setProperties(props);
    		} 
            
	        in = FileLocator.Find("Startup.webl");
	        if (in != null) {
	            AutoStreamReader s = new AutoStreamReader(in, "", "UTF8");
                BufferedReader di = new BufferedReader(s);
                machine = new Machine("Startup.webl", di, args, arg0, startuplog);
             } else
                machine = new Machine("", null, args, arg0, startuplog);
        } catch (FileNotFoundException e) {
            // never occurs
        } catch (IOException e) {
            throw new ServletException("WebL panic: Error while running Startup.webl, " + e
                + System.getProperty("line.separator") + startuplog.toString());               // return syntax errors
        } catch (WebLException e) {
            throw new ServletException("WebL panic: Exception while running Startup.webl, " + e.report());
        }  	    
	}
	
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException    {
        doService(req, res);
    }
	
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException    {
        doService(req, res);
    }
    
    public void doService(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException    {
        String module = "", name = "";
        try {
            StringTokenizer T = new StringTokenizer(req.getPathInfo(), "/");
            String modvar = T.nextToken();
            
            T = new StringTokenizer(modvar, "_");
            module = T.nextToken();
            name = T.nextToken();
            
            Logger startuplog = new StringLog(false);
            
            startuplog.debugln("Module: " + module + " Name: " + name);
            
            Module M = machine.loadModule(module, true, startuplog);
            if (M == null)
                throw new ServletException("Error while loading WebL module " + module +
                    System.getProperty("line.separator") + startuplog.toString());
                
            VarExpr ivar = M.importVariable(name);
            if (ivar == null)
                error(res, "No such exported variable: " + name);
            else {
                Expr e = ivar.lookup(null);
                if (e instanceof FunExpr) {
                    FunExpr fun = (FunExpr)e;
                    if (fun.args.size() == 2) {
                        Context newcontext = new Context(fun.c, null, fun.scope, Program.Str(module + "_" + name));
                        newcontext.binding[0] = BuildRequestObject(req);
                        newcontext.binding[1] = BuildResponseObject(res);
                        fun.body.eval(newcontext);
                        UpdateResponse(res, newcontext.binding[1]);
                    } else {
                        error(res, "Exported function does not have the required 2 formal arguments");
                    }
                } else {
                    error(res, "Exported variable " + name + " is not a function");
                }
            }
        } catch (FormatException e) {
            error(res, e.toString());
        } catch (NoSuchElementException e) {
            error(res, "Illegal arguments");
        } catch (FileNotFoundException e) {
            error(res, e.toString());
        } catch (IOException e) {
            error(res, e.toString());
        } catch (WebLException e) {
            error(res, e.report());
        } catch (Exception e) {
            error(res, e.toString());
            e.printStackTrace();
        }
	}
	
	public String getServletInfo() {
        return "WebL servlet";    
    }
    
    public ObjectExpr BuildRequestObject(HttpServletRequest req) {
        ObjectExpr obj = new ObjectExpr();
        def(obj, "method", req.getMethod());
        def(obj, "requestURI", req.getRequestURI());
        def(obj, "servletpath", req.getServletPath());
        def(obj, "pathinfo", req.getPathInfo());
        def(obj, "pathtranslated", req.getPathTranslated());
        def(obj, "querystring", req.getQueryString());
        def(obj, "remoteuser", req.getRemoteUser());
        def(obj, "authtype", req.getAuthType());
        
        def(obj, "remoteaddr", req.getRemoteAddr());
        def(obj, "remotehost", req.getRemoteHost());
        def(obj, "scheme", req.getScheme());
        def(obj, "servername", req.getServerName());
        obj.def("serverport", Program.Int(req.getServerPort()));
        def(obj, "protocol", req.getProtocol());
        def(obj, "contenttype", req.getContentType());
        
        ObjectExpr header = new ObjectExpr();
        Enumeration e = req.getHeaderNames();
        while (e.hasMoreElements()) {
            String n = (String)e.nextElement();
            String v = (String)req.getHeader(n);
            if (v == null)
                header.def(n.toLowerCase(), Program.nilval);
            else
                header.def(n.toLowerCase(), Program.Str(v));
        }
        obj.def("header", header);
        
        ObjectExpr param = new ObjectExpr();
        e = req.getParameterNames();
        while (e.hasMoreElements()) {
            String n = (String)e.nextElement();
            String[] V = req.getParameterValues(n);
            if (V.length > 1) {
                ListExpr L = new ListExpr();
                for (int i = 0; i < V.length; i++)
                    L = L.Append(Program.Str(V[i]));
                param.def(n, L);
            } else
                param.def(n, Program.Str(V[0]));
        }
        obj.def("param", param);
        
        ObjectExpr cobj = new ObjectExpr();
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for(int i=0; i < cookies.length; i++) {
                Cookie c = cookies[i];
                def(cobj, c.getName(), c.getValue());
            }
        }
        obj.def("cookies", cobj);
        
        return obj;
    }
    
    public ObjectExpr BuildResponseObject(HttpServletResponse res) {
        ObjectExpr obj = new ObjectExpr();
        obj.def("statuscode", Program.Int(200));
        obj.def("statusmsg", Program.Str("OK"));
        obj.def("result", Program.Str("empty page"));
        obj.def("mimetype", Program.Str("text/html"));
        obj.def("cookies", new ObjectExpr());
        obj.def("header", new ObjectExpr());
        return obj;
    }
    
    public void UpdateResponse(HttpServletResponse res, Expr o) throws FormatException, IOException {
        if (o instanceof ObjectExpr) {
            ObjectExpr obj = (ObjectExpr)o;
            res.setStatus((int)getInt(obj, "statuscode"), getStr(obj, "statusmsg"));
            
            // set headers
            ObjectExpr header = getObj(obj, "header");
            Enumeration e = header.EnumKeys();
            while (e.hasMoreElements()) {
                Expr n = (Expr)e.nextElement();
                res.setHeader(n.print(), header.get(n).print());
            }
            
            // set cookies
            ObjectExpr t = getObj(obj, "cookies");
            e = t.getContent();
            while (e.hasMoreElements()) {
                Expr n = (Expr)e.nextElement();
                res.addCookie(MakeCookie(n, t.get(n)));
            }
            
            res.setContentType(getStr(obj, "mimetype"));
            PrintWriter out = res.getWriter();
            out.print(getStr(obj, "result"));
        }
    }
    
    private Cookie MakeCookie(Expr name, Expr o) throws FormatException {
        if (o != null && o instanceof ObjectExpr) {
            ObjectExpr obj = (ObjectExpr)o;
            Cookie C = new Cookie(name.print(), getStr(obj, "value"));
            C.setComment(getStr(obj, "comment"));
            C.setDomain(getStr(obj, "domain"));
            C.setPath(getStr(obj, "path"));
            C.setMaxAge((int)getInt(obj, "maxage"));
            C.setVersion((int)getInt(obj, "version"));
            return C;
        } else
            throw new FormatException("Cookies need to be of type object; got instead " + o);
    }
    
    private void def(ObjectExpr obj, String fld, String val) {
        if (val == null)
            obj.def(fld, Program.nilval);
        else
            obj.def(fld, Program.Str(val));
    }
    
    private String getStr(ObjectExpr obj, String fld) throws FormatException {
        Expr val = obj.get(fld);
        if (val == null)
            throw new FormatException("Expected an object field called " + fld + " in " + obj);
        else if (val == Program.nilval)
            return "";
        else if (val instanceof StringExpr) 
            return ((StringExpr)val).val();
        else
            return val.print();
    }
    
    private long getInt(ObjectExpr obj, String fld) throws FormatException {
        Expr val = obj.get(fld);
        if (val == null)
            throw new FormatException("Expected an object field called " + fld + " in " + obj);
        else if (val instanceof IntExpr) 
            return ((IntExpr)val).val;
        else
            throw new FormatException("Expected an object field called " + fld + " of type int in " + obj);        
    }
    
    private ObjectExpr getObj(ObjectExpr obj, String fld) throws FormatException {
        Expr val = obj.get(fld);
        if (val == null)
            throw new FormatException("Expected an object field called " + fld + " in " + obj);
        else if (val instanceof ObjectExpr) 
            return (ObjectExpr)val;
        else
            throw new FormatException("Expected an object field called " + fld + " of type object in " + obj);        
    }
    
    private boolean getBool(ObjectExpr obj, String fld) throws FormatException {
        Expr val = obj.get(fld);
        if (val == null)
            throw new FormatException("Expected an object field called " + fld + " in " + obj);
        else if (val instanceof BooleanExpr) 
            return val == Program.trueval;
        else
            throw new FormatException("Expected an object field called " + fld + " of type bool in " + obj);        
    }
    
    void error(HttpServletResponse res, String msg) throws IOException {
        res.setContentType("text/html");
        res.setStatus(500, "Internal Server Error");
        
        PrintWriter out = res.getWriter();
        out.println("<HEAD><TITLE>500 Internal Server Error</TITLE></HEAD><BODY>");
        out.println("<h1>500 WebL Internal Server Error</h1><pre>");
        out.println(msg);
    	out.println("<pre></BODY>");
    	out.close();
    	
    	Log.debugln("[Error: " + msg + "]");
    }
    
}

class FormatException extends Exception
{
    public FormatException(String msg) {
        super(msg);
    }
}



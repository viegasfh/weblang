package webl.page.net;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import webl.page.html.*;
import webl.page.xml.*;
import webl.page.plain.*;

import webl.util.*;
import java.util.*;
import java.net.*;
import java.io.*;

public class Net
{
    static public Page GetURL(String url, Expr params, ObjectExpr headers, ObjectExpr options) throws NetException {
        Log.debugln("[GET " + url + "]");
        URLConnection c = Connect("GET", url, params, headers, options);
        return FetchPage(c, options);
    }
    
    static public Page PostURL(String url, Expr params, ObjectExpr headers, ObjectExpr options) throws NetException, IOException {
        Log.debugln("[POST " + url + "]");
        URLConnection c = Connect("POST", url, params, headers, options);
        return FetchPage(c, options);
    }
    
    static public Page HeadURL(String url, Expr params, ObjectExpr headers, ObjectExpr options) throws NetException {
        Log.debugln("[HEAD " + url + "]");
        URLConnection c = Connect("HEAD", url, params, headers, options);
        
        try {
    	    c.getInputStream();
    	    // throw away the data (jinyu: works for FTP)
        } catch (FileNotFoundException e) {
    	    throw new NetException(404, "file not found, " + e);
        } catch (IOException e) {
    	    throw new NetException(400, "connection error, " + e);
        }
        
        Page P = new Page(null, Page.HTML);
        P.appendPCData("No content");
        
        P.def("URL", Program.Str(c.getURL().toExternalForm()));
        getHeaders(c, P);
        return P;
    }
    
    static public ObjectExpr GetURLDownload(String url, Expr params, ObjectExpr headers, ObjectExpr options, String filename) throws NetException {
        Log.debugln("[GET " + url + "]");
        URLConnection c = Connect("GET", url, params, headers, options);
        return Download(c, options, filename);
    }
    
    static public ObjectExpr PostURLDownload(String url, Expr params, ObjectExpr headers, ObjectExpr options, String filename) throws NetException, IOException {
        Log.debugln("[POST " + url + "]");
        URLConnection c = Connect("POST", url, params, headers, options);
        return Download(c, options, filename);
    }
    
    static private boolean GetAutoRedirectFlag(ObjectExpr obj) {
        if (obj == null)
            return true;
            
        Expr r = obj.get("autoredirect");
        if (r == Program.falseval)
            return false;
        else
            return true;
    }
    
    static private boolean GetNonCompliantPOSTRedirectFlag(ObjectExpr obj) {
        if (obj == null)
            return true;
            
        Expr r = obj.get("noncompliantPOSTredirect");
        if (r == Program.falseval)
            return false;
        else
            return true;
    }
    
    static public URLConnection Connect(String operation, String url, Expr params, ObjectExpr headers, ObjectExpr options) throws NetException {
        URLConnection con = null;
        URL Url;
        
        if (!operation.equalsIgnoreCase("POST") && params != null)
            url += EncodeParams(operation, params);
            
        try {
            Url = new URL(url);
        } catch (MalformedURLException e) {
            throw new NetException(400, "Malformed URL, " + e);
        } catch (IOException e) {
            throw new NetException(400, "DNS lookup failed, " + e);
        }
        try {
            int redirects = 0;
            do {
                con = Url.openConnection();
                con.setUseCaches(true);
                
                String cookiedbid = GetStringVal(options, "cookiedb");
                
                if (con instanceof HttpURLConnection) {
                    HttpURLConnection c = (HttpURLConnection)con;
                    c.setFollowRedirects(/*GetAutoRedirectFlag(options)*/false); 
                    c.setDoInput(true);
                    c.setAllowUserInteraction(false);
                    
                    if (Url.getProtocol().equals("http")) // fix by jinyu
            		    // ftp through proxy is an instance of HttpURLConnection !!!
                        c.setRequestMethod(operation);  // fix by jinyu
                    
                    // set the header info
                    if (headers != null) {
                        Enumeration e = headers.EnumKeys();
                        while(e.hasMoreElements()) {
                            Expr n = (Expr)e.nextElement();
                            Expr v = headers.get(n);
                            if (v instanceof ListExpr) {
                                Enumeration ee = ((ListExpr)v).getContent();
                                while (ee.hasMoreElements()) {
                                    c.setRequestProperty(n.print(), ((Expr)ee.nextElement()).print());
                                }
                            } else 
                                c.setRequestProperty(n.print(), v.print());
                        }
                    }   
                    if (c.getRequestProperty("cookie") == null) // no cookie override, use our internal cookie database
                        SetCookie(cookiedbid, Url, c);
                }
                
                if (operation.equalsIgnoreCase("POST") && params != null) {
                    con.setDoOutput(true);
                    PrintWriter W = new PrintWriter(con.getOutputStream());
                    W.print(EncodeParams(operation, params));
                    W.close();
                }
                
                con.connect();
                
                if (con instanceof HttpURLConnection) {
                    HttpURLConnection c = (HttpURLConnection)con;
                    int statuscode = c.getResponseCode();
                    
                    // check (possible multiple) cookies
                    int i = 1;
                    String key = con.getHeaderFieldKey(i);
                    while (key != null) {
                        if (key.equalsIgnoreCase("set-cookie")) {
                            String cookiestr = con.getHeaderField(i);
                            SaveCookie(cookiedbid, Url, cookiestr);
                        }
                        key = con.getHeaderFieldKey(++i);
                    }        
                    
                    // Check redirects
                    if (statuscode >=300 && statuscode <= 305 && GetAutoRedirectFlag(options)) { 
                        String loc = c.getHeaderField("Location");
                        if (loc != null) {
                            Log.debugln("[Redirect to " + loc + "]");
                            try {
                                Url = new URL(Url, loc);
                            } catch (MalformedURLException e) {
                                throw new NetException(400, "Malformed redirect location URL, " + e);
                            } catch (IOException e) {
                                throw new NetException(400, "DNS lookup failed, " + e);
                            }
                            redirects++;
                            
                            // In section 9.3 of the HTTP 1.0 specification and section 10.3 of the HTTP 1.1. 
                            // specification, it mentions that some existing HTTP 1.0 user agents will erroneously
                            // change a redirected POST request into a GET request. So we will too.
                            if (operation.equalsIgnoreCase("POST") && GetNonCompliantPOSTRedirectFlag(options)) {
                                operation = "GET"; params = null;
                            }
                            continue;
                        }
                    }
                    
                    if (statuscode < 200 || statuscode >= 300)     // some sort of error occurred
                        throw new NetException(statuscode, c.getResponseMessage(), con);
                }
                return con;
            } while (redirects < 5);
            throw new NetException(400, "too many HTTP redirects", con);
        } catch (FileNotFoundException e) {
            throw new NetException(404, "file not found, " + e, con);
        } catch (IOException e) {
            throw new NetException(400, "connection error, " + e);
        }    
    }

    static private String GetStringVal(ObjectExpr obj, String fld) {
        if (obj == null)
            return null;
            
        Expr r = obj.get(fld);
        if (r != null && r instanceof StringExpr)
            return ((StringExpr)r).val();
        return null;
    }
    
    static private ParserInterface GetParser(MIMEType M) throws NetException {
        String mimetype = M.getTypeSlashSubType().toLowerCase();
        if (mimetype.equals("text/plain")) {
            return new TextPlainParser();
        } else if (mimetype.equals("text/html")) {
            return new HTMLParser();
        } else if (mimetype.equals("text/xml")) {
            return new XMLParser();    
        } else if (mimetype.equals("application/xml")) {
            return new XMLParser();    
        } else
            throw new NetException(400, "Unsupported mime type " + M.toString());
    }
    
    static public Page FetchPage(URLConnection c, ObjectExpr options) throws NetException {
        try {
            // first determine the mime-type, so we can pick the appropriate parser
            String mimetype = GetStringVal(options, "mimetype");    // check if we have an override
            if (mimetype == null) mimetype = c.getContentType();
            if (mimetype == null || mimetype.equals(""))
                throw new NetException(400, "Cannot determine the mime type of the page (please use mime override)");
            
            MIMEType M = new MIMEType(mimetype);
            
            // locate the parser according to the mimetype
            ParserInterface P = GetParser(M);
            
            // determine the appropriate character set, so we can construct the correct Reader
            String charset = GetStringVal(options, "charset");
            if (charset == null) charset = M.getParameter("charset");
            if (charset == null) charset = "";
            
            AutoStreamReader R = new AutoStreamReader(c.getInputStream(), charset, P.DefaultCharset());
            
            String url = c.getURL().toExternalForm();
            Page page = P.Parse(R, url, options);
            page.def("URL", Program.Str(url));
            getHeaders(c, page);
            return page;
        } catch (IllegalMIMETypeException e) {
            throw new NetException(400, "illegal MIME type, " + e);
        } catch (IOException e) {
            throw new NetException(400, "download error, " + e);
        }
    }
    
    static public Page FetchPage(Reader R, String url, String mimetype, ObjectExpr options) throws NetException {
        try {
            // first determine the mime-type, so we can pick the appropriate parser
            String mimetype0 = GetStringVal(options, "mimetype");    // check if we have an override
            if (mimetype0 == null) mimetype0 = mimetype;
            if (mimetype0 == null || mimetype0.equals(""))
                throw new NetException(400, "Cannot determine the mime type of the page (please use mime override)");
            
            MIMEType M = new MIMEType(mimetype0);
            
            // locate the parser according to the mimetype
            ParserInterface P = GetParser(M);
            
            Page page = P.Parse(R, url, options);
            page.def("URL", Program.Str(url));
            return page;
        } catch (IllegalMIMETypeException e) {
            throw new NetException(400, "illegal MIME type, " + e);
        } catch (IOException e) {
            throw new NetException(400, "download error, " + e);
        }        
    }
    
    static public Page FetchPage(InputStream in, String url, String mimetype, ObjectExpr options) throws NetException {
        try {
            // first determine the mime-type, so we can pick the appropriate parser
            String mimetype0 = GetStringVal(options, "mimetype");    // check if we have an override
            if (mimetype0 == null) mimetype0 = mimetype;
            if (mimetype0 == null || mimetype0.equals(""))
                throw new NetException(400, "Cannot determine the mime type of the page (please use mime override)");
            
            MIMEType M = new MIMEType(mimetype0);
            
            // locate the parser according to the mimetype
            ParserInterface P = GetParser(M);
            
            // determine the appropriate character set, so we can construct the correct Reader
            String charset = GetStringVal(options, "charset");
            if (charset == null) charset = M.getParameter("charset");
            if (charset == null) charset = "";
            
            AutoStreamReader R = new AutoStreamReader(in, charset, P.DefaultCharset());
            
            Page page = P.Parse(R, url, options);
            page.def("URL", Program.Str(url));
            return page;
        } catch (IllegalMIMETypeException e) {
            throw new NetException(400, "illegal MIME type, " + e);
        } catch (IOException e) {
            throw new NetException(400, "download error, " + e);
        }        
    }
    
    static public ObjectExpr Download(URLConnection c, ObjectExpr options, String filename) throws NetException {
        try {
            InputStream is = c.getInputStream(); 
            FileOutputStream os = new FileOutputStream(filename);
            
            byte[] buf = new byte[4096];
            int n = is.read(buf);
            while (n > 0) {
                os.write(buf, 0, n);
                n = is.read(buf);
            }
            os.flush();
            os.close();
        } catch (IOException e) {
            throw new NetException(400, "download error, " + e, c);            
        }
		
		String url = c.getURL().toExternalForm();
		
        // retrieve all the header fields
        ObjectExpr obj = new ObjectExpr();
        getHeaders(c, obj);
        obj.def("URL", Program.Str(url));
        return obj;    
    }    
    
    private static void getHeaders(URLConnection con, ObjectExpr obj) {
        int i = 1;
        String key = con.getHeaderFieldKey(i);
        while (key != null) {
            Expr keyL = Program.Str(key.toLowerCase());
            Expr val = Program.Str(con.getHeaderField(i));
            
            Expr v = obj.get(keyL);
            if (v == null) {
                // do nothing
            } else if (v instanceof StringExpr) {
                ListExpr L = new ListExpr(v);
                val = L.Append(val);
            } else if (v instanceof ListExpr) {
                val = ((ListExpr)v).Append(val);
            } else
                throw new InternalError("unexpected header value type");
            
            obj.def(keyL, val);
            key = con.getHeaderFieldKey(++i);
        }
    }    
    
    static String EncodeParams(String operation, Expr arg) {
        if (arg != null) {
            if (arg instanceof ObjectExpr) {
                ObjectExpr obj = (ObjectExpr)arg;
                if (!obj.empty()) {
                    StringBuffer s = new StringBuffer();
                    if (!operation.equalsIgnoreCase("POST"))
                        s.append("?");
                        
                    Enumeration e = obj.EnumKeys();
                    
                    while(e.hasMoreElements()) {
                        Expr n = (Expr)e.nextElement();
                        Expr v = (Expr)obj.get(n);
                        if (v == Program.nilval)
                            continue;
                        else if (v instanceof ListExpr) {
                            Enumeration e2 = ((ListExpr)v).getContent();
                            while (e2.hasMoreElements()) {
                                Expr v2 = (Expr)e2.nextElement();
                                s.append(URLEncoder.encode(n.print()) + "=" + URLEncoder.encode(v2.print()));
                                if (e2.hasMoreElements()) 
                                    s.append('&');
                            }
                        } else 
                            s.append(URLEncoder.encode(n.print()) + "=" + URLEncoder.encode(v.print()));
                            
                        if (e.hasMoreElements()) 
                            s.append('&');
                    }
                    return s.toString();
                } // else fail
            } else if (arg instanceof StringExpr) {  
                if (!operation.equalsIgnoreCase("POST"))
                    return "?" + ((StringExpr)arg).val();
                else
                    return ((StringExpr)arg).val();
            }
        }
        return "";
    }     
    
    static public String ResolveBASE(String docurl, String base) {
        try {
            URL x = new URL(new URL(docurl), base);
            return x.toString();
        } catch (MalformedURLException m) {
            Log.debugln("[Malformed BASE URL : " + m + "]");
            return base;
        }
    }

    static public String ResolveHREF(String urlbase, String href) {
        if(href.startsWith("#"))
            return href;
        else {
            try {
                URL x;
                if (urlbase.equals(""))
                    x = new URL(href);
                else
                    x = new URL(new URL(urlbase), href);
                return x.toString();
            } catch (MalformedURLException m) {
                Log.debugln("[Malformed HREF URL : " + m + "]");
                return href;
            }
        }
    }    
    
    static private CookieDB     defaultdb = new CookieDB();
    static private Hashtable    cookiedbs = new Hashtable();
    static private Object       csync = new Object();
    
    static public void SetCookie(String dbid, URL url, HttpURLConnection c) {
        if (dbid == null || dbid.equals("")) {
            defaultdb.SetCookie(url, c);
        } else {
            synchronized(csync) {
                CookieDB db = (CookieDB)cookiedbs.get(dbid);
                if (db != null) 
                    db.SetCookie(url, c);
            }
        }
    }
    
    static public void SaveCookie(String dbid, URL url, String cookiestr) {
        if (dbid == null || dbid.equals("")) {
            defaultdb.SaveCookie(url, cookiestr);
        } else {
            CookieDB db;
            synchronized(csync) {
                db = (CookieDB)cookiedbs.get(dbid);
                if (db == null) {
                    db = new CookieDB();
                    cookiedbs.put(dbid, db);
                }
            }
            db.SaveCookie(url, cookiestr);
        }
    }
    
    static public CookieDB getCookieDB(String dbid) {
        if (dbid == null || dbid.equals("")) {
            return defaultdb;
        } else {
            CookieDB db;
            synchronized(csync) {
                db = (CookieDB)cookiedbs.get(dbid);
                if (db == null) {
                    db = new CookieDB();
                    cookiedbs.put(dbid, db);
                }
            }
            return db;
        }
    }
}

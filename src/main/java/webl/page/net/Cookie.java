package webl.page.net;

import java.net.*;
import java.util.*;
import webl.util.Log;
import weblx.url.*;

/**
 * This class represents an http cookie as specified in
 * <a href="http://home.netscape.com/newsref/std/cookie_spec.html">Netscape's
 * cookie spec</a>
 */
 
public class Cookie 
{
    public String   cookiename = null;
    public String   cookieval = null;
    public String   expires = null;
    public String   domain = null;
    public String   path = null;
    public boolean  secure = false;
    public String   domainkey = null;
    public int      pathlen;
    
    public Cookie   next;   
    
    public Cookie(URL url, String header) throws IllegalCookieException, MalformedURL {
        StringTokenizer T = new StringTokenizer(header, ";");
        SetNameVal(T.nextToken());
        
        while (T.hasMoreTokens()) 
            SetFieldVal(T.nextToken());
            
        SetDefaults(url);
    }
    
    public Cookie(String header) throws IllegalCookieException, MalformedURL {
        StringTokenizer T = new StringTokenizer(header, ";");
        SetNameVal(T.nextToken());
        
        while (T.hasMoreTokens()) 
            SetFieldVal(T.nextToken());
            
        SetDefaults(null);
    }
    
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(cookiename).append('=').append(cookieval).append("; ");
        s.append("domain").append('=').append(domain).append("; ");
        s.append("path").append('=').append(path).append("; ");
        s.append("expires").append('=').append(expires).append("; ");
        if (secure)
            s.append("secure");
        return s.toString();
    }
    
    private void SetNameVal(String field) throws IllegalCookieException {
        int pos = field.indexOf('=');
        
        if (pos != -1 && pos != field.length() - 1) {
            cookiename = field.substring(0, pos).trim();
            cookieval = field.substring(pos + 1).trim();
        } else
            throw new IllegalCookieException("cookie format " + field);
    }
    
    private void SetFieldVal(String field) throws IllegalCookieException {
        field = field.trim();
        int pos = field.indexOf('=');
        
        if (field.equalsIgnoreCase("secure"))
            secure = true;
        else if (pos != -1 && pos != field.length() - 1) {
            String attr = field.substring(0, pos).trim();
            String val = field.substring(pos + 1).trim();
            
            if (attr.equalsIgnoreCase("expires"))
                expires = val;
            else if (attr.equalsIgnoreCase("domain"))
                domain = val;
            else if (attr.equalsIgnoreCase("path"))
                path = val;
            // ignore unknown field
        } else if (!field.equals(""))
            throw new IllegalCookieException("cookie format " + field);
    }
    
    
    private void SetDefaults(URL url) throws MalformedURL {
        if (url != null) {
            URLSplitter S = new URLSplitter(url.toExternalForm());
            
            if (domain == null)
                domain = S.host;
            if (expires == null) 
                expires = "";
            if (path == null) {
                path = ExtractPath(S.path);  // should also split off filename
                if (path.equals(""))
                    path = "/";
            }
        }
        domainkey = HashKey(domain);
        pathlen = path.length();
    }
 
    public boolean HostMatch(URL url) {
        String host = url.getHost();
        return host.endsWith(domain);
    }
    
    // does this cookie match the URL ?
    public boolean Match(URL url) {
        if (HostMatch(url)) {
            String hpath = url.getFile();
            return hpath.startsWith(path);
        }
        return false;
    }
    
    private String ExtractPath(String name) {
        int pos = name.lastIndexOf('/');
        if (pos > 0) 
            return name.substring(0, pos);
        else
            return name;
    }
    
    public static String HashKey(String domain) {
        StringTokenizer T = new StringTokenizer(domain, ".");
        String a=null, b=null;
        
        while (T.hasMoreTokens()) {
            a = b;
            b = T.nextToken();
        }
        return a + "." + b;
    }
    
}



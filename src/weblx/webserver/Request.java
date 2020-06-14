package weblx.webserver;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class Request extends ObjectExpr
{
    public Request() {
        def("header", new ObjectExpr());
        def("param", new ObjectExpr());
    }
    
    private String getStr(String key) {
        Expr v = get(key);
        if (v != null)
            return ((StringExpr)v).val();
        else
            return "";
    }
    
    public void setMethod(String method) {
        def("method", Program.Str(method.toUpperCase()));
    }
    
    public String getMethod() {
        return getStr("method");
    }
    
    public void setProtocol(String protocol) {
        def("protocol", Program.Str(protocol.toUpperCase()));
    }
    
    public String getProtocol() {
        return getStr("protocol");
    }
    
    public void setURI(String uri) {
        def("uri", Program.Str(uri));
        
        String path = uri;
        
        // strip off the query string, if any
        int pos = uri.indexOf('?');
        if (pos != -1) {
            path = uri.substring(0, pos);
            setQueryFields(uri.substring(pos + 1));
        }
        def("path", Program.Str(path));
    }
    
    public String getURI() {
        return getStr("uri");
    }
    
    public String getQuery() {
        return getStr("query");
    }
    
    public String getPath() {
        return getStr("path");
    }
    
    public void setContents(String contents) {
        def("contents", Program.Str(contents));
        if (getStr("method").equals("POST"))
            setQueryFields(contents);
    }
    
    public String getContents() {
        return getStr("contents");
    }
    
    private void defVal(ObjectExpr obj, String name, String value) {
        Expr val = Program.Str(value);
        Expr v = obj.get(name);
        if (v == null) {
            // do nothing
        } else if (v instanceof StringExpr) {
            ListExpr L = new ListExpr(v);
            val = L.Append(val);
        } else if (v instanceof ListExpr) {
            val = ((ListExpr)v).Append(val);
        } else
            throw new InternalError("unexpected header value type");
            
        obj.def(name, val);
    }
    
    public void setHeader(String name, String value) {
        ObjectExpr header = (ObjectExpr)get("header");
        defVal(header, name, value);
    }
    
    private void setQueryFields(String q) {
        def("query", Program.Str(q));
        ObjectExpr param = (ObjectExpr)get("param");
        
        StringTokenizer T = new StringTokenizer(q, "&");
        while (T.hasMoreTokens()) {
            String s = T.nextToken();
            
            StringTokenizer S = new StringTokenizer(s, "=");
            
            String name = S.nextToken();
            String value;
            try {
                value = S.nextToken().replace('+', ' ');
            } catch(NoSuchElementException e) {
                value = "";
            }
            defVal(param, decode(name), decode(value));
        }
    }

    private String decode(String str) {
        StringBuffer result = new StringBuffer();
        int l = str.length();
        for (int i = 0; i < l; ++i) {
            char c = str.charAt(i);
            if (c == '%' && i + 2 < l) {
                char c1 = str.charAt(i + 1);
                char c2 = str.charAt(i + 2);
                if (isHexit(c1) && isHexit(c2)) {
                    result.append((char)(hexit(c1) * 16 + hexit(c2)));
                    i += 2;
                } else
                    result.append(c);
            } else
                result.append(c);
        }
        return result.toString();
   }

   private boolean isHexit( char c ) {
        String legalChars = "0123456789abcdefABCDEF";
        return (legalChars.indexOf(c) != -1 );
   }
   
   private int hexit( char c ) {
        if ( c >= '0' && c <= '9' )
            return c - '0';
        if ( c >= 'a' && c <= 'f' )
            return c - 'a' + 10;
        if ( c >= 'A' && c <= 'F' )
            return c - 'A' + 10;
        return 0;       // shouldn't happen, we're guarded by isHexit()
   }
}
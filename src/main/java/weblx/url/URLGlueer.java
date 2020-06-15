package weblx.url;

import webl.lang.*;
import webl.lang.expr.*;

public class URLGlueer
{
    public String   scheme;
    public String   host;
    public long     port = 0;
    public String   path;
    public String   query;
    public String   ref;
    public String   url;
    
    public String   user;
    public String   password;
    public char     ftptype = 0;
    
    public URLGlueer(ObjectExpr obj) throws MalformedURL {
        scheme = GetS(obj, "scheme");
        host = GetS(obj, "host");
        port = GetI(obj, "port");
        path = GetS(obj, "path");
        query = GetS(obj, "query");
        ref = GetS(obj, "ref");
        url = GetS(obj, "url");
        user = GetS(obj, "user");
        password = GetS(obj, "password");
        ftptype = GetC(obj, "type");

    }
    
    public String toString() {
        StringBuffer s = new StringBuffer();
        if (scheme.equals("http")) {
            s.append("http://");
            s.append(host);
            if (port != 0) {
                s.append(':');
                s.append(port);
            }
            if (!path.equals(""))
                s.append(path);
            if (!query.equals("")) 
                s.append(query);
            if (!ref.equals("")) 
                s.append('#').append(ref);
        } else if (scheme.equals("ftp")) {
            s.append("ftp://");
            if (!user.equals("")) {
                s.append(user);
                if (!password.equals("")) 
                    s.append(':').append(password);
            }
            s.append(host);
            if (port != 0) 
                s.append(':').append(port);
            if (!path.equals(""))
                s.append(path);
            if (ftptype != 0) 
                s.append(";type=").append(ftptype);
        } else if (scheme.equals("file")) {
            s.append("file:");
            if (!host.equals("")) {
                s.append("//").append(host);
            }
            s.append(path);
            if (!ref.equals("")) 
                s.append('#').append(ref);
        } else if (!scheme.equals("")) {
            s.append(url);
        } else if (!url.equals("")) {
            s.append(url);
        } else {
            if (!path.equals(""))
                s.append(path);
            if (!query.equals("")) 
                s.append(query);
            if (!ref.equals("")) 
                s.append('#').append(ref);
        }
        return s.toString();        
    }
    
    private String GetS(ObjectExpr obj, String fld) throws MalformedURL {
        Expr V = (Expr)obj.get(fld);
        if (V == null)
            return "";
        else if (V instanceof StringExpr)
            return ((StringExpr)V).val();
        else
            throw new MalformedURL("value of field " + fld + " expected to be of type string");
    }
    
    private long GetI(ObjectExpr obj, String fld) throws MalformedURL {
        Expr V = (Expr)obj.get(fld);
        if (V == null)
            return 0;
        else if (V instanceof IntExpr)
            return ((IntExpr)V).val;
        else
            throw new MalformedURL("value of field " + fld + " expected to be of type int");
    }    
    
    private char GetC(ObjectExpr obj, String fld) throws MalformedURL {
        Expr V = (Expr)obj.get(fld);
        if (V == null)
            return 0;
        else if (V instanceof CharExpr)
            return ((CharExpr)V).ch;
        else
            throw new MalformedURL("value of field " + fld + " expected to be of type char");
    }      
}

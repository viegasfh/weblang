package webl.page.net;

import java.net.*;
import webl.lang.*;
import webl.lang.expr.*;

public class NetException extends Exception
{
    public int statuscode;
    public String msg;
    public URLConnection connection = null;
    
    public NetException(int statuscode, String msg) {
        this.statuscode = statuscode;
        this.msg = msg;
    }
    
    public NetException(int statuscode, String msg, URLConnection c) {
        this(statuscode, msg);
        connection = c;
    }
    
    public WebLException makeWebLException(Context c, Expr callsite, NetException e) {
        ObjectExpr obj = new ObjectExpr();
        
        obj.def("msg", Program.Str(msg));
        obj.def("statuscode", Program.Int(statuscode));
        obj.def("type", Program.Str("NetException"));
        
        if (e.connection != null) {
            ObjectExpr hd = new ObjectExpr();
            obj.def("headers", hd);
            
            int i = 1;
            String key = e.connection.getHeaderFieldKey(i++);
            while (key != null) {
                String val = e.connection.getHeaderField(key);
                hd.def(key, Program.Str(val));
                key = e.connection.getHeaderFieldKey(i++);
            }
        }  
        return new WebLException(c, callsite, obj);        
    }    
} 
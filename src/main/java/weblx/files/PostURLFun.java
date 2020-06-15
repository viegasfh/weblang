package weblx.files;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import webl.page.net.*;
import java.util.*;
import java.io.*;

//
// PostURL(url:string, filename:string, [ param, [ headers]] )
//
public class PostURLFun extends AbstractFunExpr
{
    public String toString() {
        return "<Files_PostURL>";
    }
    
    private Expr ObjectOrStringArg(Context c, Vector args, Expr callsite, int param) throws WebLException {
        Expr r = ((Expr)(args.elementAt(param))).eval(c);
        if (r instanceof ObjectExpr)
            return r;
        else if (r instanceof StringExpr)
            return r;
        else
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects an object or string as argument " + param);
    } 
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        Expr params = null;
        ObjectExpr headers = null, options = null;
        String mime = null; 
        
        if (args.size() < 2 || args.size() > 5)
            throw new WebLException(c, callsite, "ArgumentError", "wrong number of arguments passed to " + this + " function");
        
        String url = StringArg(c, args, callsite, 0);
        String filename = StringArg(c, args, callsite, 1);
        
        // get the params and header object arguments
        if (args.size() > 2) {
            params = ObjectOrStringArg(c, args, callsite, 2);
            if (args.size() > 3) {
                headers = ObjectArg(c, args, callsite, 3);
                if (args.size() > 4)
                    options = ObjectArg(c, args, callsite, 4);
            }
        }
        
        try {
            return Net.PostURLDownload(url, params, headers, options, filename);
        } catch (NetException e) {
            throw e.makeWebLException(c, callsite, e);
        } catch (IOException e) {
            throw new WebLException(c, callsite, "IOError", "IO error in postpage function");
        }
    }
}

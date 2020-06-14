package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import webl.page.net.*;
import java.util.*;
import java.io.*;

//
// PostURL(url:string, [ param, [ headers [, mime]]] )
//
public class PostURLFun extends AbstractFunExpr
{
    public String toString() {
        return "<PostURL>";
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
        
        if (args.size() < 1 || args.size() > 4)
            throw new WebLException(c, callsite, "ArgumentError", "wrong number of arguments passed to " + this + " function");
        
        String url = StringArg(c, args, callsite, 0);
        
        // get the params and header object arguments
        if (args.size() > 1) {
            params = ObjectOrStringArg(c, args, callsite, 1);
            
            if (args.size() > 2) {
                headers = ObjectArg(c, args, callsite, 2);
                if (args.size() > 3)
                    options = ObjectArg(c, args, callsite, 3);
            }
        }
        
        try {
            Page page = Net.PostURL(url, params, headers, options);
            return page;
        } catch (NetException e) {
            throw e.makeWebLException(c, callsite, e);
        } catch (IOException e) {
            throw new WebLException(c, callsite, "IOException", "IO exception in " + this.toString() + " function");
        }
    }
}
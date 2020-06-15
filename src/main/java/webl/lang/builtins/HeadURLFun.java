package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import webl.page.net.*;
import java.util.*;

//
// HeadURL(url:string, [ param, [ headers ]] )
//
public class HeadURLFun extends AbstractFunExpr
{
    
    public String toString() {
        return "<HeadURL>";
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
        ObjectExpr headers = null;
        
        if (args.size() < 1 || args.size() > 3)
            throw new WebLException(c, callsite, "ArgumentError", "wrong number of arguments passed to " + this + " function");
        
        String url = StringArg(c, args, callsite, 0);
        
        // get the params and header object arguments
        if (args.size() > 1) {
            params = ObjectOrStringArg(c, args, callsite, 1);
            
            if (args.size() > 2) {
                headers = ObjectArg(c, args, callsite, 2);
            }
        }
        
        try {
            Page page = Net.HeadURL(url, params, headers, null);
            return page;
        } catch (NetException e) {
            throw e.makeWebLException(c, callsite, e);
        }
    }
}
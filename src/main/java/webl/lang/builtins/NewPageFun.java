package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.net.*;
import webl.util.*;
import java.util.*;
import java.io.*;


public class NewPageFun extends AbstractFunExpr
{
    public String toString() {
        return "<NewPage>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        
        String s = StringArg(c, args, callsite, 0);
        String mime = StringArg(c, args, callsite, 1);
        try {
            Reader R = new BufferedReader(new StringReader(s));
    	    return Net.FetchPage(R, "", mime, null);
    	} catch (NetException e) {
    	    throw e.makeWebLException(c, callsite, e);
    	}
    }
}
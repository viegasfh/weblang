package weblx.cookies;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.net.*;
import webl.util.*;
import java.util.*;
import java.io.*;

public class SaveFun extends AbstractFunExpr
{
    public String toString() {
        return "<Cookies_Save>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        if (args.size() < 1 || args.size() > 2)
            throw new WebLException(c, callsite, "ArgumentError", this + " function expects one or two arguments");
        
        String fname = StringArg(c, args, callsite, 0);
        String dbid = null;
        if (args.size() == 2) 
            dbid = StringArg(c, args, callsite, 1);
            
        try {
            FileOutputStream f = new FileOutputStream(fname);
            Writer W = new OutputStreamWriter(f);
            Net.getCookieDB(dbid).Save(W);
            W.close();
        } catch(IOException E) {
            throw new WebLException(c, callsite, "SaveError", "unable to write output to " + fname + ", " + E);
        }
        return Program.nilval;
    }
}

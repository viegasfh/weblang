package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import webl.dtd.*;
import java.util.*;
import java.io.*;

public class ExpandCharEntitiesFun extends AbstractFunExpr
{
    public String toString() {
        return "<ExpandCharEntities>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        try {
            if (args.size() == 1) {             // ExpandCharEntities(string): string
                String s = StringArg(c, args, callsite, 0);
                return Program.Str(Expand(null, s));
            } else if (args.size() == 2) {      // ExpandCharEntities(page, string): string
                Expr p = ((Expr)(args.elementAt(0))).eval(c);   
                if (p instanceof Page) {
                    String s = StringArg(c, args, callsite, 1);
                    return Program.Str(Expand(((Page)p).dtd, s));
                } else 
                    throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a page as first argument");
            } else
                throw new WebLException(c, callsite, "ArgumentError", "wrong number of arguments, " 
                    + this + " function expects one or two argument(s)");
        } catch (IOException e) {
             throw new WebLException(c, callsite, "IOException", e.toString());
        }
    }
    
    String Expand(DTD dtd, String s) throws IOException {
        if (dtd == null) 
            dtd = Catalog.OpenHTML40();
        return dtd.ExpandCharEntities(s);
    }
}
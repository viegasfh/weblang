package weblx.str;

import webl.lang.*;
import webl.lang.expr.*;
import webl.lang.builtins.*;
import java.util.*;
import java.util.regex.*;

public class ContainsFun extends AbstractFunExpr
{
    public String toString() {
        return "<Str_Contains>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        if (args.size() != 2)
            throw new WebLException(c, callsite, "ArgumentError", "wrong number of arguments passed to match function");
            
        Expr s = ((Expr)(args.elementAt(0))).eval(c);
        if (!(s instanceof StringExpr))
            throw new WebLException(c, callsite, "ArgumentError", "contains function expects a string as first argument");

        Expr r = ((Expr)(args.elementAt(1))).eval(c);
        if (!(r instanceof StringExpr))
            throw new WebLException(c, callsite, "ArgumentError", "contains function expects a string as second argument");
        
        String source = StringArg(c, args, callsite, 0);
        String target = StringArg(c, args, callsite, 1);

        if (source.contains(target)) {
            return Program.trueval;
        } else
            return Program.falseval;
    }
}
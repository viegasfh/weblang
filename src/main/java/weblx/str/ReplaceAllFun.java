package weblx.str;

import webl.lang.*;
import webl.lang.expr.*;
import webl.lang.builtins.*;
import java.util.*;
import java.util.regex.*;

public class ReplaceAllFun extends AbstractFunExpr
{
    public String toString() {
        return "<Str_ReplaceAll>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        if (args.size() != 3)
            throw new WebLException(c, callsite, "ArgumentError", "wrong number of arguments passed to match function");
            
        Expr s = ((Expr)(args.elementAt(0))).eval(c);
        if (!(s instanceof StringExpr))
            throw new WebLException(c, callsite, "ArgumentError", "replaceAll function expects a string as first argument");

        Expr t = ((Expr)(args.elementAt(1))).eval(c);
        if (!(t instanceof StringExpr))
            throw new WebLException(c, callsite, "ArgumentError", "replaceAll function expects a regular expression string as second argument");

         Expr r = ((Expr)(args.elementAt(1))).eval(c);
        if (!(r instanceof StringExpr))
            throw new WebLException(c, callsite, "ArgumentError", "replaceAll function expects a string as third argument");
       
        String source = StringArg(c, args, callsite, 0);
        String regex = StringArg(c, args, callsite, 1);
        String replacement = StringArg(c, args, callsite, 2);

        return Program.Str(source.replaceAll(regex, replacement));
    }
}
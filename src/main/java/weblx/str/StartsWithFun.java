package weblx.str;

import webl.lang.*;
import webl.lang.expr.*;
import webl.lang.builtins.*;
import java.util.*;
import java.util.regex.*;

public class StartsWithFun extends AbstractFunExpr
{
    public String toString() {
        return "<StartsWith>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        if (args.size() != 2)
            throw new WebLException(c, callsite, "ArgumentError", "wrong number of arguments passed to match function");
            
        Expr s = ((Expr)(args.elementAt(0))).eval(c);
        if (!(s instanceof StringExpr))
            throw new WebLException(c, callsite, "ArgumentError", "match function expects a string as first argument");

        Expr r = ((Expr)(args.elementAt(1))).eval(c);
        if (!(r instanceof StringExpr))
            throw new WebLException(c, callsite, "ArgumentError", "match function expects a regular expression string as second argument");
        

        Pattern pattern;
        try {
            pattern = Pattern.compile(((StringExpr)r).val());
        } catch(PatternSyntaxException e) {
            throw new WebLException(c, callsite, "ArgumentError", "malformed regular expression pattern");
        }
        
        Matcher matcher = pattern.matcher(((StringExpr)s).val());
        
        if (matcher.find()) {
            if (matcher.start() == 0)
                return Program.trueval;
            else
                return Program.falseval;
        } else
            return Program.falseval;
    }
}
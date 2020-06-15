package weblx.java;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class LengthFun extends AbstractFunExpr
{
    public String toString() {
        return "<Java_Length>";
    }

    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        
        Expr s = ((Expr)(args.elementAt(0))).eval(c);
        if (!(s instanceof JavaArrayExpr))
            throw new WebLException(c, callsite, "AgumentError", toString() + " expects a java array as first argument");
            
        try {
            return Program.Int(((JavaArrayExpr)s).size());
        } catch (IllegalArgumentException e) {
            throw new WebLException(c, callsite, "IllegalArgumentError", e.toString());
        }
    }
}
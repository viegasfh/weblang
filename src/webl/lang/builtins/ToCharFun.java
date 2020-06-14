package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;
import java.lang.*;

public class ToCharFun extends AbstractFunExpr
{
    public String toString() {
        return "<ToChar>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        Expr t = ((Expr)(args.elementAt(0))).eval(c);
        
        try {
            if (t instanceof CharExpr) {
                return t;
            } else if (t instanceof IntExpr) {
                long f = ((IntExpr)t).val;
                return Program.Chr((char)f);
            }
        } catch(NumberFormatException n) {
            throw new WebLException(c, callsite, "ArgumentError", "conversion to character failed");
        }
        throw new WebLException(c, callsite, "ArgumentError", "tochar function expects an integer as argument");
    }
}

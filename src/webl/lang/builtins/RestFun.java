package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class RestFun extends AbstractFunExpr
{
    public String toString() {
        return "<Rest>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        Expr t = ((Expr)(args.elementAt(0))).eval(c);
        if (t instanceof ListExpr) {
            try {
                return ((ListExpr)t).Rest();
            } catch(IndexOutOfBoundsException e) {
                return new ListExpr();
            }
        }
        throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a list as argument");
    }
}

package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class SortFun extends AbstractFunExpr
{
    public String toString() {
        return "<Sort>";
    }

    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        
        Expr list = ((Expr)args.elementAt(0)).eval(c);
        if (list instanceof ListExpr) {
            Expr fun = ((Expr)args.elementAt(1)).eval(c);
            if (fun instanceof AbstractFunExpr) {
                return ((ListExpr)list).sort(c, callsite, (AbstractFunExpr)fun);
            } else
                throw new WebLException(c, callsite, "ArgumentError", "second argument is not a function");
        } else        
            throw new WebLException(c, callsite, "ArgumentError", "first argument is not a list");
    }
}

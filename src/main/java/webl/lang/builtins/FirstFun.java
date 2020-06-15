package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class FirstFun extends AbstractFunExpr
{
    public String toString() {
        return "<First>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        Expr t = ((Expr)(args.elementAt(0))).eval(c);
        if (!(t instanceof ListExpr))
            throw new WebLException(c, callsite, "ArgumentError", this + " function expects a list as argument");
        if ( ((ListExpr)t).getSize() == 0)
            throw new WebLException(c, callsite, "EmptyList", "cannot apply first to an empty list");
        return ((ListExpr)t).getElementAt(0);
    }
}

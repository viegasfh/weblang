package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;


public class ThrowFun extends AbstractFunExpr
{
    public String toString() {
        return "<Throw>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);

        Expr e = ((Expr)(args.elementAt(0))).eval(c);
        if (e instanceof ObjectExpr) 
            throw new WebLException(c, callsite, (ObjectExpr)e);
        else
            throw new WebLException(c, callsite, "ArgumentError", "object expected as first argument of throw");
    }
}

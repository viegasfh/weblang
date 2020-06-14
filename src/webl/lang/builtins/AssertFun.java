package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class AssertFun extends AbstractFunExpr
{
    public String toString() {
        return "<Assert>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        Expr e = ((Expr)(args.elementAt(0))).eval(c);
        if (e != Program.trueval)
            throw new WebLException(c, callsite, "AssertFailed", "Assertion failed");
        return Program.trueval;
    }
}

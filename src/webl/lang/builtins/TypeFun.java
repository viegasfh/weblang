package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class TypeFun extends AbstractFunExpr
{
    public String toString() {
        return "<Type>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);

        ValueExpr t = (ValueExpr)((Expr)(args.elementAt(0))).eval(c);
        return Program.Str(t.getTypeName());
    }
}
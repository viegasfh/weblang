package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class DeleteFieldFun extends AbstractFunExpr
{
    public String toString() {
        return "<DeleteField>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        Expr o = ((Expr)(args.elementAt(0))).eval(c);
        if (!(o instanceof ObjectExpr)) 
            throw new WebLException(c, callsite, "ArgumentError", "DeleteField function expect an object as first argument");
        
        Expr f = ((Expr)(args.elementAt(1))).eval(c);
        ((ObjectExpr)o).remove(f);
        return Program.nilval;
    }
}
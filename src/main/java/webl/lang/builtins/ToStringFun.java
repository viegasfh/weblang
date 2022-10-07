package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;
import java.lang.*;

public class ToStringFun extends AbstractFunExpr
{
    public String toString() {
        return "<ToString>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        Expr t = ((Expr)(args.elementAt(0))).eval(c);
        
        return Program.Str(t.print());
    }
}

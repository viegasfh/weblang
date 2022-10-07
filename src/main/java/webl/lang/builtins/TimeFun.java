package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

// return the time to execute an expression
public class TimeFun extends AbstractFunExpr
{
    public String toString() {
        return "<Time>";
    }
    
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);

        long t = System.currentTimeMillis();
        try {
            Expr e = ((Expr)(args.elementAt(0))).eval(c);
        } catch (Exception e) {}
        t = System.currentTimeMillis() - t;
        return Program.Int(t);
    }
}

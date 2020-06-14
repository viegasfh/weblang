package webl.lang.builtins;

import java.lang.*;
import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

// sleep for a certain number of ms
public class SleepFun extends AbstractFunExpr
{
    public String toString() {
        return "<Sleep>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        
        long period = IntArg(c, args, callsite, 0);
        try {
            Thread.sleep(period);
        } catch(InterruptedException e) {
            throw new WebLException(c, callsite, "Interrupt", "Sleep function interrupted");
        }
        return Program.nilval;
    }
}

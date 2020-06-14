package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.util.Log;
import java.util.*;

public class RetryFun extends AbstractFunExpr
{
    public String toString() {
        return "<Retry>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        Expr body = (Expr)(args.elementAt(0));
        
        while (true) {
            try {
                return body.eval(c);
            } catch (WebLException e) {
            }
        }
    }
}

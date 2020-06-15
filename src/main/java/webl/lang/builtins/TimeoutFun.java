package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.util.Log;
import java.util.*;

public class TimeoutFun extends AbstractFunExpr
{
    public String toString() {
        return "<Timeout>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        long timeout = IntArg(c, args, callsite, 0);
        Expr body = (Expr)(args.elementAt(1));
        return WebLThread.TimeoutCombinator(c, callsite, timeout, body);
    }
}

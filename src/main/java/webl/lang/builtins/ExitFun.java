package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class ExitFun extends AbstractFunExpr
{
    public String toString() {
        return "<Exit>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        
        System.exit((int)IntArg(c, args, callsite, 0));
        return Program.nilval;
    }
}

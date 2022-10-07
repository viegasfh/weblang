package weblx.str;

import webl.lang.*;
import webl.lang.expr.*;
import webl.lang.builtins.*;
import java.util.*;

public class LastIndexOfFun extends AbstractFunExpr
{
    public String toString() {
        return "<Str_LastIndexOf>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        String pat = StringArg(c, args, callsite, 0);
        String s = StringArg(c, args, callsite, 1);
        return Program.Int(s.lastIndexOf(pat));
    }
}
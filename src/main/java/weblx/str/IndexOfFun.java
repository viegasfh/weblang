package weblx.str;

import webl.lang.*;
import webl.lang.expr.*;
import webl.lang.builtins.*;
import java.util.*;

public class IndexOfFun extends AbstractFunExpr
{
    public String toString() {
        return "<Str_IndexOf>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        String pat = StringArg(c, args, callsite, 0);
        String s = StringArg(c, args, callsite, 1);
        return Program.Int(s.indexOf(pat));
    }
}
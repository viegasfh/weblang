package weblx.str;

import webl.lang.*;
import webl.lang.expr.*;
import webl.lang.builtins.*;
import java.util.*;

public class ReplaceFun extends AbstractFunExpr
{
    public String toString() {
        return "<Replace>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 3);
        char from = CharArg(c, args, callsite, 1);
        char to = CharArg(c, args, callsite, 2);
        return Program.Str(StringArg(c, args, callsite, 0).replace(from, to));
    }
}
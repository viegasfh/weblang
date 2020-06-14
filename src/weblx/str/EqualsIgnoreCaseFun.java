package weblx.str;

import webl.lang.*;
import webl.lang.expr.*;
import webl.lang.builtins.*;
import java.util.*;

public class EqualsIgnoreCaseFun extends AbstractFunExpr
{
    public String toString() {
        return "<EqualsIgnoreCase>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        String s = StringArg(c, args, callsite, 0);
        String t = StringArg(c, args, callsite, 1);
        if (s.equalsIgnoreCase(t))
            return Program.trueval;
        else
            return Program.falseval;
    }
}
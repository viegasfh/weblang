package weblx.str;

import webl.lang.*;
import webl.lang.expr.*;
import webl.lang.builtins.*;
import java.util.*;

public class ToLowerCaseFun extends AbstractFunExpr
{
    public String toString() {
        return "<ToLowerCase>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        return Program.Str(StringArg(c, args, callsite, 0).toLowerCase());
    }
}
package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;
import java.lang.*;

public class TrapFun extends AbstractFunExpr
{
    public String toString() {
        return "<Trap>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        try {
            Expr t = ((Expr)(args.elementAt(0))).eval(c);
            return Program.nilval;
        } catch (WebLException e) {
            ObjectExpr obj = e.MakeObject();
            obj.def("trace", Program.Str(e.report()));
            return obj;
        }
    }
}
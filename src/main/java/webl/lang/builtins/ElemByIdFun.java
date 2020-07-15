package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;

public class ElemByIdFun extends AbstractFunExpr
{
    public String toString() {
        return "<ElemById>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        
        Expr p = ((Expr)(args.elementAt(0))).eval(c);   
        if (p instanceof Page) {
            String s = StringArg(c, args, callsite, 1);
            Piece pce = ((Page)p).getElemById(s);
            if (pce == null) 
                return Program.nilval;
            else
                return pce;
        } else
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a page as first argument");

    }
}
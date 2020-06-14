package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;

public class ParentFun extends AbstractFunExpr
{
    public String toString() {
        return "<Parent>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        
        Expr p = ((Expr)(args.elementAt(0))).eval(c);  
        try {
            if (p instanceof Piece) {
                Piece pce = ((Piece)p).page.Parent((Piece)p);
                if (pce == null) 
                    return Program.nilval;
                else
                    return pce;
            } else
                throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a piece as first argument");
        } catch (TypeCheckException e) {
            throw new Error("internal error");
        }
    }
}
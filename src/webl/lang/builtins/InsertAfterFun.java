package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;

public class InsertAfterFun extends AbstractFunExpr
{
    public String toString() {
        return "<InsertAfter>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        
        Expr t = ((Expr)(args.elementAt(0))).eval(c);  
        if (!(t instanceof TagExpr))
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a tag as first argument");

        PieceSet set = PieceSet.castExpr(((Expr)(args.elementAt(1))).eval(c));
        if (set == null) 
            throw new WebLException(c, this, "ArgumentError", this + " method expects a piece or pieceset as 2nd argument");
  
         ((TagExpr)t).page.insertAfter(((TagExpr)t).tag, set);
         return Program.nilval;
    }
}
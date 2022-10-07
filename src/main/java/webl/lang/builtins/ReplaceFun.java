package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;

public class ReplaceFun extends AbstractFunExpr
{
    public String toString() {
        return "<Replace>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        Expr r = ((Expr)(args.elementAt(0))).eval(c);
        PieceSet x = PieceSet.castExpr(r);
        r = ((Expr)(args.elementAt(1))).eval(c);
        PieceSet y = PieceSet.castExpr(r);
        if (x == null || y == null) 
            throw new WebLException(c, this, "ArgumentError", "replace function expects pieces or piecesets as argument");
            
        try {            
            x.page.replace(x, y);
        } catch (TypeCheckException e) {
            throw new Error("internal error");
        }            
        return Program.nilval;        
    }
}
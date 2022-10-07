package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;

public class DeleteFun extends AbstractFunExpr
{
    public String toString() {
        return "<Delete>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        
        Expr p = ((Expr)(args.elementAt(0))).eval(c);   
        try {
            if (p instanceof PieceSet) {
                ((PieceSet)p).page.deleteRange((PieceSet) p);
            } else if (p instanceof Piece) {
                ((Piece)p).page.deleteRange(PieceSet.castExpr(p));
            } else
                throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a piece or pieceset as first argument");
            return Program.nilval;
        } catch(TypeCheckException e) {
            throw new Error("internal error");
        }
    }
}
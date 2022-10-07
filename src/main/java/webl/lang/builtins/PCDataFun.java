package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;

public class PCDataFun extends AbstractFunExpr
{
    public String toString() {
        return "<PCData>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        try {
            Expr p = ((Expr)(args.elementAt(0))).eval(c);   
            if (p instanceof Page) {
                return ((Page)p).GetPCDataPieces();
            } else if (p instanceof Piece) {
                return ((Piece)p).page.GetPCDataPieces((Piece)p);
            } else
                throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a page or piece as argument");
        } catch (TypeCheckException e) {
            throw new InternalError("Piece/Page mismatch");
        }
    }
}
package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;

public class PageFun extends AbstractFunExpr
{
    public String toString() {
        return "<Page>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        
        Expr p = ((Expr)(args.elementAt(0))).eval(c);   
        if (p instanceof Piece) {
            return ((Piece)p).page;
        } else if (p instanceof TagExpr) {
            return ((TagExpr)p).page;
        } else
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a piece or tag as first argument");
    }
}
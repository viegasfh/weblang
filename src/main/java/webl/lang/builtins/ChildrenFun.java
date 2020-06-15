package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;

public class ChildrenFun extends AbstractFunExpr
{
    public String toString() {
        return "<Children>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        
        Expr p = ((Expr)(args.elementAt(0))).eval(c);  
        try {
            if (p instanceof Piece) {
                return ((Piece)p).page.Children((Piece)p);
            } else
                throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a piece as first argument");
        } catch (TypeCheckException e) {
            throw new Error("internal error");
        }
    }
}
package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;

public class ElemByClassFun extends AbstractFunExpr
{
    public String toString() {
        return "<ElemByClass>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        
        try {
            Expr p = ((Expr)(args.elementAt(0))).eval(c);   
            String s = StringArg(c, args, callsite, 1);

            if (p instanceof Page) {
                return ((Page)p).getElemByClass(s);
            } else if (p instanceof Piece) {
                return ((Piece)p).page.getElemByClass((Piece)p, s);
            } else
                throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a page or piece as first argument");
        } catch (TypeCheckException e) {
            throw new Error("internal error");
        }
    }
}
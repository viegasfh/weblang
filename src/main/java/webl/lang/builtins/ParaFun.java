package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;

public class ParaFun extends AbstractFunExpr
{
    public String toString() {
        return "<Para>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        
        Expr p = ((Expr)(args.elementAt(0))).eval(c);
        String breaktags = StringArg(c, args, callsite, 1);
        
        if (p instanceof Page) {
            return ((Page)p).getPara(breaktags);
        } else if (p instanceof Piece) {
            return ((Piece)p).page.getPara((Piece)p, breaktags);
        } else
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a page or piece as first argument");
    }
}
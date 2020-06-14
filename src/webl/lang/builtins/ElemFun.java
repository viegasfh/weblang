package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;

public class ElemFun extends AbstractFunExpr
{
    public String toString() {
        return "<Elem>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        if (args.size() < 1 || args.size() > 2)
            throw new WebLException(c, callsite, "ArgumentError", "wrong number of arguments, " 
            + this + " function expects one or two argument(s)");
        
        try {
            Expr p = ((Expr)(args.elementAt(0))).eval(c);   
            if (p instanceof Page) {
                if (args.size() == 2) {
                    String s = StringArg(c, args, callsite, 1);
                    return ((Page)p).getElem(s);
                } else
                    return ((Page)p).getElem();
            } else if (p instanceof Piece) {
                if (args.size() == 2) {
                    String s = StringArg(c, args, callsite, 1);
                    return ((Piece)p).page.getElem((Piece)p, s);
                } else
                    return ((Piece)p).page.getElem((Piece)p);
            } else
                throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a page or piece as first argument");
        } catch (TypeCheckException e) {
            throw new Error("internal error");
        }

    }
}
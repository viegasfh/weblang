package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;

public class FirstElemFun extends AbstractFunExpr
{
    public String toString() {
        return "<FirstElem>";
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
                    Piece pce = ((Page)p).getFirstElem(s);
                    if (pce == null)
                        return Program.nilval;
                    else
                        return pce;

                } else
                    throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a second argument for the name of the element");
            } else if (p instanceof Piece) {
                if (args.size() == 2) {
                    String s = StringArg(c, args, callsite, 1);
                    Piece pce = ((Piece)p).page.getFirstElem((Piece)p, s);
                    if (pce == null)
                        return Program.nilval;
                    else
                        return pce;
                } else
                    throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a second argument for the name of the element");
            } else
                throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a page or piece as first argument");
        } catch (TypeCheckException e) {
            throw new Error("internal error");
        }

    }
}
package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;
import java.io.*;

public class NewPieceSetFun extends AbstractFunExpr
{
    
    public String toString() {
        return "<NewPieceSet>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        
        Expr s = ((Expr)(args.elementAt(0))).eval(c);
        if (s instanceof SetExpr) {
            PieceSet R = null;
            Page page = null;
            try {
                Enumeration enum = ((SetExpr)s).getContent();
                while (enum.hasMoreElements()) {
                    Object e = enum.nextElement();
                    if (e instanceof Piece) {
                        if (R == null) {
                            page = ((Piece)e).page;
                            R = new PieceSet(page);
                        }
                        R.insert((Piece)e);
                    } else
                        throw new WebLException(c, callsite, "NotAPiece", "the set argument to NewPieceSet must only contain pieces");
                }
                if (R == null)
                    throw new WebLException(c, callsite, "EmptySet", "the set argument to NewPieceSet must contain at least one piece");
                else
                    return R;
            } catch(TypeCheckException e) {
                throw new WebLException(c, callsite, "NotSamePage", "the set argument to NewPieceSet must only contain pieces belonging to the same page");
            }
        } else if (s instanceof Page) {
            return new PieceSet((Page)s);
        } else
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a set or page as argument");
    }
}
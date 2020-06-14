package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;

public class ContentFun extends AbstractFunExpr
{
    public String toString() {
        return "<Content>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        
        Piece res;
        
        Expr p = ((Expr)(args.elementAt(0))).eval(c);   
        if (p instanceof Page) {
            res = ((Page)p).getContentPiece();
        } else if (p instanceof Piece) {
            res = ((Piece)p).page.getContentPiece((Piece)p);
        } else
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a page or piece as first argument");
        if (res == null) 
            throw new WebLException(c, callsite, "NoContent", "piece or page has no content");
        else
            return res;
    }
}
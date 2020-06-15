package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;

public class PrettyFun extends AbstractFunExpr
{
    public String toString() {
        return "<Pretty>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        
        Expr p = ((Expr)(args.elementAt(0))).eval(c);   
        if (p instanceof Page) {
            return Program.Str(((Page)p).getPrettyMarkup());
        } else if (p instanceof Piece) {
            return Program.Str(((Piece)p).page.getPrettyMarkup((Piece)p));;
        } else
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a page or piece as first argument");
    }
}
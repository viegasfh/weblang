package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;

public class TextFun extends AbstractFunExpr
{
    public String toString() {
        return "<Text>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        Expr p = null;
        boolean insertspaces = false;
        
        if (args.size() == 1)
            p = ((Expr)(args.elementAt(0))).eval(c);
        else
            throw new WebLException(c, callsite, "ArgumentError", "wrong number of arguments passed to " + this + " function");
        
        if (p instanceof Page) {
            return Program.Str(((Page)p).getText());
        } else if (p instanceof Piece) {
            return Program.Str(((Piece)p).page.getText((Piece)p));
        } else
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a page or piece as first argument");
    }
}
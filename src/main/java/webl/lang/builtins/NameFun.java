package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;

public class NameFun extends AbstractFunExpr
{
    public String toString() {
        return "<Name>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        
        Expr p = ((Expr)(args.elementAt(0))).eval(c);   
        if (p instanceof Piece) {
            String name = ((Piece)p).name;
            if (name == null) name = "";
            return Program.Str(name);
        } else
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a piece as first argument");
    }
}
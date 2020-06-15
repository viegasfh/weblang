package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;

// return the size of an object
public class SizeFun extends AbstractFunExpr
{
    public String toString() {
        return "<Size>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        
        Expr e = ((Expr)args.elementAt(0)).eval(c);
        if (e instanceof ListExpr) 
            return Program.Int(((ListExpr)e).getSize());
        else if (e instanceof SetExpr)
            return Program.Int(((SetExpr)e).getSize());
        else if (e instanceof StringExpr)
            return Program.Int(((StringExpr)e).val().length());
        else if (e instanceof PieceSet)
            return Program.Int(((PieceSet)e).getSize());
            
        throw new WebLException(c, callsite, "ArgumentError", "argument is not a list, set, string, or piece-set");
    }
}

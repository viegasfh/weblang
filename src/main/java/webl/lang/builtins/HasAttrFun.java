package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;

public class HasAttrFun extends AbstractFunExpr
{
    public String toString() {
        return "<HasAttr>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        if (args.size() != 2)
            throw new WebLException(c, callsite, "ArgumentError", "wrong number of arguments, " 
            + this + " function expects two argument(s)");
        
        Expr p = ((Expr)(args.elementAt(0))).eval(c);   
        if (p instanceof Piece) {
          String s = StringArg(c, args, callsite, 1);
          if (((Piece)p).getAttr(s) != null)
            return Program.trueval;
          else
            return Program.falseval;
        } else
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a piece as first argument");
    }
}
package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;
import java.io.*;

public class FlattenFun extends AbstractFunExpr
{
    public String toString() {
        return "<Flatten>";
    }

    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        
        Expr s = ((Expr)(args.elementAt(0))).eval(c);
        if (s instanceof PieceSet) {
            return PieceSet.OpFlatten((PieceSet)s);
        }
        throw new WebLException(c, this, "ArgumentError", this + " function expects a PieceSet as argument");
    }
}
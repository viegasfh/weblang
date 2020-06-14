package weblx.java;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class SetFun extends AbstractFunExpr
{
    public String toString() {
        return "<Java_Set>";
    }

    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 3);
        
        Expr s = ((Expr)(args.elementAt(0))).eval(c);
        if (!(s instanceof JavaArrayExpr))
            throw new WebLException(c, callsite, "AgumentError", toString() + " expects a java array as first argument");
            
        long index = IntArg(c, args, callsite, 1);
        
        Expr val = ((Expr)(args.elementAt(2))).eval(c);
        try {
            boolean ok = ((JavaArrayExpr)s).set((int)index, val);
            if (!ok)
                throw new WebLException(c, callsite, "SetError", "illegal array assigned operation");
            return val;
        } catch (IllegalArgumentException e) {
            throw new WebLException(c, callsite, "IllegalArgumentError", e.toString());
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new WebLException(c, callsite, "IndexError", "index out of bounds " + index);
        }
    }
}
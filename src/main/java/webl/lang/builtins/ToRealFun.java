package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;
import java.lang.*;

public class ToRealFun extends AbstractFunExpr
{
    public String toString() {
        return "<ToReal>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        Expr t = ((Expr)(args.elementAt(0))).eval(c);
        
        try {
            if (t instanceof StringExpr) {
                Double d = new Double(((StringExpr)t).val());
                return Program.Real(d.doubleValue());
            } else if (t instanceof CharExpr) {
                char ch = ((CharExpr)t).ch;
                long l = (long)ch & 0xFFFF;
                return Program.Real((double)l);
            } else if (t instanceof IntExpr) {
                Long l = new Long(((IntExpr)t).val);   
                return Program.Real(l.doubleValue());
            } else if (t instanceof RealExpr) {
                return t;
            }
        } catch(NumberFormatException n) {
            throw new WebLException(c, callsite, "ArgumentError", "conversion to real failed");
        }
        throw new WebLException(c, callsite, "ArgumentError", this + " function expects a string, char or integer as argument");
    }
}

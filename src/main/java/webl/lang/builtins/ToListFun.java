package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;
import java.lang.*;

public class ToListFun extends AbstractFunExpr
{
    public String toString() {
        return "<ToList>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        Expr t = ((Expr)(args.elementAt(0))).eval(c);
        if (t instanceof ContentEnumeration) {
            ListExpr R = new ListExpr();
            
            Enumeration enum = ((ContentEnumeration)t).getContent();
            while (enum.hasMoreElements()) {
                Expr x = (Expr)(enum.nextElement());
                R = R.Append(x);
            }
            return R;
        } else
            throw new WebLException(c, callsite, "ArgumentError", this.toString() + " function expects a set, list, string, object or piece-set as argument");
    }
}

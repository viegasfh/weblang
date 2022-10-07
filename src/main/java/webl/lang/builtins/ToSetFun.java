package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;
import java.lang.*;

public class ToSetFun extends AbstractFunExpr
{
    public String toString() {
        return "<ToSet>";
    }

    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        Expr t = ((Expr)(args.elementAt(0))).eval(c);
        if (t instanceof ContentEnumeration) {
            SetExpr R = new SetExpr();

            Enumeration enumeration = ((ContentEnumeration)t).getContent();
            while (enumeration.hasMoreElements()) {
                Expr x = (Expr)(enumeration.nextElement());
                R.DestructivePut(x);
            }
            return R;
        } else
            throw new WebLException(c, callsite, "ArgumentError", this.toString() + " function expects a set, list, string, object or piece-set as argument");
    }
}

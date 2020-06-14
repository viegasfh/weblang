package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class CloneFun extends AbstractFunExpr
{
    public String toString() {
        return "<Clone>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        ObjectExpr obj = new ObjectExpr();

        for(int i = 0; i < args.size(); i++) {
            Expr o = ((Expr)(args.elementAt(i))).eval(c);
            if (o instanceof ObjectExpr) {
                ObjectExpr O = (ObjectExpr)o;
                Enumeration e = O.EnumKeys();

                while(e.hasMoreElements()) {
                    Expr n = (Expr)e.nextElement();
                    obj.def(n, O.get(n));
                }
            } else
                throw new WebLException(c, callsite, "ArgumentError", "clone function expects objects as arguments");
        }
        return obj;
    }
}


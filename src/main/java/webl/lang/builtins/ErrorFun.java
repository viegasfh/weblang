package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

// return the size of an object
public class ErrorFun extends AbstractFunExpr
{
    public String toString() {
        return "<Error>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        StringBuffer s = new StringBuffer();
        for(int i = 0; i < args.size(); i++) {
            Expr e = ((Expr)(args.elementAt(i))).eval(c);
            s.append(e.print());
        }
        System.err.print(s.toString());
        return Program.nilval;
    }
}
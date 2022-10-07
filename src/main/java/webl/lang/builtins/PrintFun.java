package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.util.Log;
import java.util.*;

public class PrintFun extends AbstractFunExpr
{
    public String toString() {
        return "<Print>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        StringBuffer s = new StringBuffer();
        for(int i = 0; i < args.size(); i++) {
            Expr e = ((Expr)(args.elementAt(i))).eval(c);
            s.append(e.print());
        }
        Log.print(s.toString());
        return Program.nilval;
    }
}

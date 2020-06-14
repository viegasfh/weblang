package weblx.str;

import webl.lang.*;
import webl.lang.expr.*;
import webl.lang.builtins.*;
import java.util.*;

public class SplitFun extends AbstractFunExpr
{
    public String toString() {
        return "<Str_Split>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        String s = StringArg(c, args, callsite, 0);
        String chars = StringArg(c, args, callsite, 1);
        
        ListExpr L = new ListExpr();
        
        StringTokenizer T = new StringTokenizer(s, chars);
        while (T.hasMoreTokens()) {
            String t = T.nextToken();
            L = L.Append(Program.Str(t));
        }
        return L;
    }
}
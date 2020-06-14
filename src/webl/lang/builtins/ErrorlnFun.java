package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class ErrorlnFun extends AbstractFunExpr
{
    public String toString() {
        return "<ErrorLn>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        BasicThread.Check();
        StringBuffer s = new StringBuffer();
        for(int i = 0; i < args.size(); i++) {
            Expr e = ((Expr)(args.elementAt(i))).eval(c);
            s.append(e.print());
        }
        System.err.println(s.toString());
        return Program.nilval;
    }
}

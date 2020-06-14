package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;
import java.io.*;

public class EvalFun extends AbstractFunExpr
{
    public String toString() {
        return "<Eval>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        String str = StringArg(c, args, callsite, 0);
        Reader R = new BufferedReader(new StringReader(str));
        try {
             Expr result = c.scope.machine.Exec("<an eval function>", R);
             if (result == null)
                throw new WebLException(c, callsite, "SyntaxError", "cannot evaluate due to syntax error in argument");
             return result;
        } catch (IOException e) {
            throw new WebLException(c, callsite, "IOException", "eval function threw an IOException");
        } catch (WebLReturnException e) {
            throw new WebLException(c, callsite, "ReturnException", 
                "A return statement was executed outside of a function or method while executing eval, " + e);
       
        }      
    }

}

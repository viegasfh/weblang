package weblx.files;

import webl.lang.*;
import webl.lang.expr.*;
import webl.util.*;
import java.util.*;
import java.io.*;

public class EvalFileFun extends AbstractFunExpr
{
    public String toString() {
        return "<EvalFile>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        String filename = StringArg(c, args, callsite, 0);
        
        try {
            File F = new File(filename);
            FileInputStream in = new FileInputStream(F);
            AutoStreamReader A = new AutoStreamReader(in, "", "");
            Reader R = new BufferedReader(A);
            Expr result = c.scope.machine.Exec("<an EvalFile function>", R);
            if (result == null)
                throw new WebLException(c, callsite, "SyntaxError", "cannot evaluate due to syntax error in argument");
            return result;
        } catch(FileNotFoundException e) {
            throw new WebLException(c, callsite, "FileNotFound", "unable to locate " + filename);
        } catch (IOException e) {
            throw new WebLException(c, callsite, "EvaluationError", "eval function threw an IOException");
        }       
    }

}
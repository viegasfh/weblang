package weblx.files;

import webl.lang.*;
import webl.lang.expr.*;
import webl.lang.builtins.*;
import java.io.*;
import java.util.*;

public class MkdirFun extends AbstractFunExpr
{
    public String toString() {
        return "<Files_Mkdir>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        String filename = StringArg(c, args, callsite, 0);
        
        File F = new File(filename);
        if (F.mkdirs())
            return Program.trueval;
        else
            return Program.falseval;
    }
}

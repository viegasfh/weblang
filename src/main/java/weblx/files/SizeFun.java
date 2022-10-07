package weblx.files;

import webl.lang.*;
import webl.lang.expr.*;
import webl.lang.builtins.*;
import java.io.*;
import java.util.*;

public class SizeFun extends AbstractFunExpr
{
    public String toString() {
        return "<Files_Size>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        String filename = StringArg(c, args, callsite, 0);
        
        File F = new File(filename);
        if(F.exists()) 
            return Program.Int(F.length());
        else
            throw new WebLException(c, callsite, "FileNotFound", "unable to locate " + filename);
    }
}
package weblx.str;

import webl.lang.*;
import webl.lang.expr.*;
import webl.lang.builtins.*;
import java.util.*;

public class DuplicateFun extends AbstractFunExpr
{
    public String toString() {
        return "<Duplicate>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        String repeatStr = StringArg(c, args, callsite, 0);
        long repeatTimes = IntArg(c, args, callsite, 1);
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < repeatTimes; i++)
            buffer.append(repeatStr);
        return Program.Str(buffer.toString());
    }
}
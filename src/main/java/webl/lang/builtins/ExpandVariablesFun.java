package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;
import java.io.*;

public class ExpandVariablesFun extends AbstractFunExpr
{
    public String toString() {
        return "<ExpandVariables>";
    }

    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        try {
            long skip = IntArg(c, args, callsite, 0);
            Context cc = c;
            while (skip > 0 && cc.caller != null)
                cc = cc.caller;
            return Program.Str(cc.expand(StringArg(c, args, callsite, 1)));
        } catch (IOException e) {
            throw new WebLException(c, callsite, "IOException", e.toString());
        }
    }
}
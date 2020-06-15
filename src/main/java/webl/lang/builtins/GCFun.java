package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

import java.lang.*;

public class GCFun extends AbstractFunExpr
{
    public String toString() {
        return "<GC>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        Runtime.getRuntime().gc();
        return Program.nilval;
    }
}    
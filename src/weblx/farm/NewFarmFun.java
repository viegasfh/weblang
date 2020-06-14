package weblx.farm;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class NewFarmFun extends AbstractFunExpr
{
    public String toString() {
        return "<NewFarm>";
    }

    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        
        long w = IntArg(c, args, callsite, 0);
        return new FarmExpr((int)w);
    }
}
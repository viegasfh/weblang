package weblx.url;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class GlueFun extends AbstractFunExpr
{
    public String toString() {
        return "<Url_Glue>";
    }

    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        ObjectExpr obj = ObjectArg(c, args, callsite, 0);
        try {
            URLGlueer S = new URLGlueer(obj);
            return Program.Str(S.toString());
        } catch (MalformedURL e) {
            throw new WebLException(c, callsite, "MalformedURL", e.getMessage());
        }
    }
}
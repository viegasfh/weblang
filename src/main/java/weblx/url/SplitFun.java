package weblx.url;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class SplitFun extends AbstractFunExpr
{
    public String toString() {
        return "<Url_Split>";
    }

    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        String url = StringArg(c, args, callsite, 0);
        try {
            URLSplitter S = new URLSplitter(url);
            return S.toObject();
        } catch (MalformedURL e) {
            throw new WebLException(c, callsite, "MalformedURL", e.getMessage());
        }
    }
}
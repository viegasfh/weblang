package weblx.url;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;
import java.net.*;

public class ResolveFun extends AbstractFunExpr
{
    public String toString() {
        return "<Url_Resolve>";
    }

    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        String urlbase = StringArg(c, args, callsite, 0);
        String href = StringArg(c, args, callsite, 1);
        
        if(href.startsWith("#"))
            return Program.Str(href);
        else {
            try {
                URL x;
                if (urlbase.equals(""))
                    x = new URL(href);
                else
                    x = new URL(new URL(urlbase), href);
                return Program.Str(x.toString());
            } catch (MalformedURLException m) {
                throw new WebLException(c, callsite, "MalformedURL", m.getMessage());
            }
        }
    }
}
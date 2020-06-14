package weblx.webserver;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;
import webl.lang.builtins.*;

public class PublishFun extends AbstractFunExpr
{
    public String toString() {
        return "<WebServer_Publish>";
    }

    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        
        String name = StringArg(c, args, callsite, 0);
        
        Expr t = ((Expr)(args.elementAt(1))).eval(c);
        if (!(t instanceof FunExpr))
            throw new WebLException(c, callsite, "ArgumentError", this.toString() + " function expects a user-defined function as second argument");
            
        WebServer.Publish(name, (FunExpr)t);
        return Program.nilval;
    }
}
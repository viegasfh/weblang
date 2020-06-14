package weblx.webserver;

import webl.lang.*;
import webl.lang.expr.*;
import java.io.*;
import java.util.*;
import webl.lang.builtins.*;

public class StartFun extends AbstractFunExpr
{
    public String toString() {
        return "<WebServer_Start>";
    }

    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        
        String root = StringArg(c, args, callsite, 0);
        long port = IntArg(c, args, callsite, 1);
        
        try {
            WebServer.Start(root, (int)port);
        } catch (IOException e) {
            throw new WebLException(c, callsite, "ServerError", "unable to start server," + e);
        }
        return Program.nilval;
    }
}
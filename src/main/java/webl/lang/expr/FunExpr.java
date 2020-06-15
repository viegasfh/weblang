package webl.lang.expr;

import java.util.*;
import webl.lang.*;

public class FunExpr extends AbstractFunExpr
{
    public Context c;
    public Scope scope;
    public Vector args;
    public Expr body;

    public FunExpr(Context c, Scope scope, Vector args, Expr body, int ppos) {
        super(ppos);
        this.c = c; this.scope = scope; this.args = args; this.body = body;
    }

    public Expr Apply(Context cc, Vector cargs, Expr callsite) throws WebLException {
        if (args.size() != cargs.size())
            throw new WebLException(c, callsite, "ArgumentError", "number of formals and arguments do not match");

        Context newcontext = new Context(c, cc, scope, callsite);
        for (int i = 0; i < args.size(); i ++) {
            Expr e = (Expr)(cargs.elementAt(i));
            newcontext.binding[i] = e.eval(cc);
        }
        try {
            return body.eval(newcontext);        
        } catch (WebLReturnException e) {
            return e.val;
        }
    }
    
    public String toString() {
        return "<user-defined function>";
    }
    
/*    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("fun(");

        for (int i = 0; i < args.size(); i++) {
            buf.append(args.elementAt(i));
            if (i < args.size() - 1)
                buf.append(", ");
        }
        
        buf.append(") ").append(body).append(" end");
        return buf.toString();        
    }
*/    
}
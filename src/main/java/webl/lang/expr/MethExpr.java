package webl.lang.expr;

import java.util.*;
import webl.lang.*;

public class MethExpr extends AbstractMethExpr
{
    public Context c;
    public Scope scope;
    public Vector args;
    public Expr body;

    public MethExpr(Context c, Scope scope, Vector args, Expr body, int ppos) {
        super(ppos);
        this.c = c; this.scope = scope; this.args = args; this.body = body;
    }

    public Expr Apply(Context cc, Expr self, Vector arg, Expr callsite) throws WebLException {
        if (args.size() != arg.size() + 1)
            throw new WebLException(cc, callsite, "ArgumentError", "number of formals and arguments do not match");

        Context newcontext = new Context(c, cc, scope, callsite);

        newcontext.binding[0] = self;
        for (int i = 1; i < args.size(); i ++) {
            Expr e = (Expr)(arg.elementAt(i - 1));
            newcontext.binding[i] = e.eval(cc);
        }
        try {
            return body.eval(newcontext);
        } catch (WebLReturnException e) {
            return e.val;
        }
    }
    
    public String toString() {
        return "<user defined method>";
    }
    
/*    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("meth(");

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
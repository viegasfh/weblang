package webl.lang.expr;

import java.util.*;
import webl.lang.*;

public class MethConstructorExpr extends Expr
{
    public Vector args;
    public Expr body;
    public Scope scope;
    
    public MethConstructorExpr(int ppos) {
        super(ppos);
        args = new Vector();
    }
    
    public boolean addArg(String argname) {
        if (!args.contains(argname)) {
            args.addElement(argname);
            return true;
        } else
            return false;
    }
    
    public void setBody(Expr body) {
        this.body = body;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("meth(");

        for (int i = 0; i < args.size(); i++) {
            buf.append(args.elementAt(i));
            if (i < args.size() - 1)
                buf.append(", ");
        }
        
        buf.append(")");
        buf.append(scope.toString());
        
        buf.append(body).append(" end");
        return buf.toString();
    }

    public Expr eval(Context c) {
        return new MethExpr(c, scope, args, body, ppos);
    }
}

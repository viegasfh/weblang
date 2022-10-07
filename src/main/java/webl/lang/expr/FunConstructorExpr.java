package webl.lang.expr;

import java.util.*;
import webl.lang.*;

public class FunConstructorExpr extends Expr
{
    public Vector args;
    public Expr body;
    public Scope scope;

    public FunConstructorExpr(int ppos) {
        super(ppos);
        args = new Vector();
    }

    public boolean addArg(String id) {
        if(!args.contains(id)) {
            args.addElement(id);
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
        buf.append("fun(");

        for (int i = 0; i < args.size(); i++) {
            buf.append(args.elementAt(i));
            if (i < args.size() - 1)
                buf.append(", ");
        }
        
        buf.append(")");
//        buf.append(scope.toString());
        
        buf.append(body).append(" end");
        return buf.toString();
    }

    public Expr eval(Context c) {
        return new FunExpr(c, scope, args, body, ppos);
    }
}
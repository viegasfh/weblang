package webl.lang.expr;

import webl.lang.*;
import java.util.*;

public class LockExpr extends Expr
{
    public Expr obj;
    public Expr body;

    public LockExpr(Expr obj, Expr body, int ppos) {
        super(ppos);
        this.obj = obj;
        this.body = body;
    }

    public String toString() {
        return "lock " + obj + " do " + body + " end";
    }

    public Expr eval(Context c) throws WebLException {
        Expr obj = this.obj.eval(c);
        Expr R;
        
        if (obj instanceof ObjectExpr) {
            synchronized(obj) {
                R = body.eval(c);
            }
            return R;
        } else
            throw new WebLException(c, this, "NotAnObject", "only objects can be locked");
    }
}

package webl.lang.expr;

import webl.lang.*;

public class AssignExpr extends Expr
{
    public Expr x, y;

    public AssignExpr(Expr x, Expr y, int ppos) {
        super(ppos);
        this.x = x; this.y = y;
    }

    public String toString() {
        return "(" + x + " = " + y + ")";
    }

    public Expr eval(Context c) throws WebLException {
        Expr val = y.eval(c);
        if (x instanceof VarExpr) {
            ((VarExpr)x).assign(c, val);
            return val;
        } else if (x instanceof IndexExpr) {
            IndexExpr n = (IndexExpr) x;            
            Expr lh = n.obj.eval(c);
            
            if (lh instanceof ObjectExpr) {
                Expr rh = n.index.eval(c);
                boolean res = ((ObjectExpr) lh).set(rh, val);
                if (!res)
                    throw new WebLException(c, this, "FieldError", "unknown field " + rh.toString() + " or illegal field assignment");
                return val;
            } else
                throw new WebLException(c, this, "NotAnObject", "not an object");

        } else
            throw new WebLException(c, this, "NotAVariable", "cannot perform assigment");
    }

}
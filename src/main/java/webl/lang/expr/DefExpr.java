package webl.lang.expr;
import webl.lang.*;

public class DefExpr extends Expr
{
    public IndexExpr x;
    public Expr y;

    public DefExpr(IndexExpr x, Expr y, int ppos) {
        super(ppos);
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return x + " := " + y;
    }

    public Expr eval(Context c) throws WebLException {
        Expr val = y.eval(c);
        Expr lh = x.obj.eval(c);
        if (lh instanceof ObjectExpr) {
            Expr rh = x.index.eval(c);
            boolean res = ((ObjectExpr) lh).def(rh, val);
            if(!res)
                throw new WebLException(c, this, "FieldDefinitionError", "could not define field " + rh.toString());
            return val;
        } else
            throw new WebLException(c, this, "NotAnObject", "left hand side is not a valid object");
    }
    
}
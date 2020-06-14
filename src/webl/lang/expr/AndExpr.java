package webl.lang.expr;

import webl.lang.*;

public class AndExpr extends Expr
{
    public Expr x, y;

    public AndExpr(Expr x, Expr y, int ppos) {
        super(ppos);
        this.x = x; this.y = y;
    }

    public String toString() {
        return "(" + x + " and " + y + ")";
    }

    public Expr eval(Context c) throws WebLException {
        Expr x0 = x.eval(c);
        if (x0 instanceof BooleanExpr) {
            if (x0 == Program.falseval)
                return x0;
            else {
                Expr y0 = y.eval(c);
                if (y0 instanceof BooleanExpr)
                    return y0;
            }
        }
        throw new WebLException(c, this, "OperandMismatch", "result of 'and' branch is not boolean");
    }
}
package webl.lang.expr;

import webl.lang.*;

public class DivExpr extends Expr
{
    public Expr x, y;

    public DivExpr(Expr x, Expr y, int ppos) {
        super(ppos);
        this.x = x; this.y = y;
    }

    public String toString() {
        return "(" + x + " / " + y + ")";
    }

    /*
        int x int       => real
        int x real      => real
        real x real     => real
        real x int      => real
    */
    public Expr eval(Context c) throws WebLException {
        Expr x0 = x.eval(c);
        Expr y0 = y.eval(c);
        if (x0 instanceof IntExpr) {
            if (y0 instanceof IntExpr)
                return new RealExpr(((IntExpr)x0).val * 1.0 / ((IntExpr)y0).val);
            else if (y0 instanceof RealExpr)
                return new RealExpr(((IntExpr)x0).val / ((RealExpr)y0).val);
        } else if (x0 instanceof RealExpr) {
            if (y0 instanceof RealExpr)
                return new RealExpr(((RealExpr)x0).val / ((RealExpr)y0).val);
            else if (y0 instanceof IntExpr)
                return new RealExpr(((RealExpr)x0).val / ((IntExpr)y0).val);
        }
        throw new WebLException(c, this, "OperandMismatch", "incompatible operands for / operator");
    }
}
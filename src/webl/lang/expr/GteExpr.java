package webl.lang.expr;

import webl.lang.*;

public class GteExpr extends Expr
{
    public Expr x, y;

    public GteExpr(Expr x, Expr y, int ppos) {
        super(ppos);
        this.x = x; this.y = y;
    }

    public String toString() {
        return "(" + x + " >= " + y + ")";
    }

    private BooleanExpr B(boolean b) {
        return b ? Program.trueval : Program.falseval;
    }

    /*
        int x int       => bool
        int x real      => bool
        real x real     => bool
        real x int      => bool
        str x str       => bool
        char x char     => bool
    */
    public Expr eval(Context c) throws WebLException {
        Expr x0 = x.eval(c);
        Expr y0 = y.eval(c);
        if (x0 instanceof IntExpr) {
            if (y0 instanceof IntExpr)
                return B(((IntExpr)x0).val >= ((IntExpr)y0).val);
            else if (y0 instanceof RealExpr)
                return B(((IntExpr)x0).val >= ((RealExpr)y0).val);
        } else if (x0 instanceof RealExpr) {
            if (y0 instanceof RealExpr)
                return B(((RealExpr)x0).val >= ((RealExpr)y0).val);
            else if (y0 instanceof IntExpr)
                return B(((RealExpr)x0).val >= ((IntExpr)y0).val);
        } else if (x0 instanceof StringExpr && y0 instanceof StringExpr) {
            return B(((StringExpr)x0).val().compareTo(((StringExpr)y0).val()) >= 0);
        } else if (x0 instanceof CharExpr && y0 instanceof CharExpr) {
            return B(((CharExpr)x0).ch >= ((CharExpr)y0).ch);
        }
        throw new WebLException(c, this, "OperandMismatch", "incompatible operands for >= operator");
    }
}
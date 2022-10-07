package webl.lang.expr;

import webl.lang.*;

public class NegExpr extends Expr
{
    public Expr x;

    public NegExpr(Expr x, int ppos) {
        super(ppos);
        this.x = x;
    }

    public String toString() {
        return "(- " + x + ")";
    }

    /*
        int       => int
    */
    public Expr eval(Context c) throws WebLException {
        Expr x0 = x.eval(c);

        if (x0 instanceof IntExpr)
            return new IntExpr(-((IntExpr)x0).val);
        else if (x0 instanceof RealExpr)
            return new RealExpr(-((RealExpr)x0).val);

        throw new WebLException(c, this, "OperandMismatch", "incompatible operand for unary minus");
    }
}
package webl.lang.expr;

import webl.lang.*;

public class ModExpr extends Expr
{
    public Expr x, y;

    public ModExpr(Expr x, Expr y, int ppos) {
        super(ppos);
        this.x = x; this.y = y;
    }

    public String toString() {
        return "(" + x + " mod " + y + ")";
    }

    /*
        int x int       => int
    */
    public Expr eval(Context c) throws WebLException {
        Expr x0 = x.eval(c);
        Expr y0 = y.eval(c);
        if (x0 instanceof IntExpr && y0 instanceof IntExpr)
                return new IntExpr(((IntExpr)x0).val % ((IntExpr)y0).val);
        throw new WebLException(c, this, "OperandMismatch", "incompatible operands for mod operator");
    }
}
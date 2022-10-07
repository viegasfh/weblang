package webl.lang.expr;

import webl.lang.*;

public class NotExpr extends Expr
{
    public Expr x;

    public NotExpr(Expr x, int ppos) {
        super(ppos);
        this.x = x;
    }

    public String toString() {
        return "(not " + x + ")";
    }

    /*
        int       => int
    */
    public Expr eval(Context c) throws WebLException {
        Expr x0 = x.eval(c);

        if (x0 instanceof BooleanExpr)
            if (x0 == Program.trueval)
                return Program.falseval;
            else
                return Program.trueval;

        throw new WebLException(c, this, "OperandMismatch", "incompatible operand for 'not'");
    }
}
package webl.lang.expr;

import webl.lang.*;

public class QuesExpr extends Expr
{
    public Expr x, y;

    public QuesExpr(Expr x, Expr y, int ppos) {
        super(ppos);
        this.x = x; this.y = y;
    }

    public String toString() {
        return "(" + x + " ? " + y + ")";
    }

    public Expr eval(Context c) throws WebLException {
        return WebLThread.CondCombinator(c, this, x, y);
    }
}

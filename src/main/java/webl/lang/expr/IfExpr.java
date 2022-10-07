package webl.lang.expr;

import webl.lang.*;

public class IfExpr extends Expr
{
    public Expr cond, iftrue, iffalse;

    public IfExpr(Expr cond, Expr truenode, Expr falsenode, int ppos) {
        super(ppos);
        this.cond = cond; iftrue = truenode; iffalse = falsenode;
    }

    public String toString() {
        return "if " + cond + " then " + iftrue + " else " + iffalse + " end";
    }

    public Expr eval(Context c) throws WebLException {
        Expr e = cond.eval(c);

        if (e == Program.trueval)
            return iftrue.eval(c);
        else if (e == Program.falseval)
            return iffalse.eval(c);
        else
            throw new WebLException(c, this, "GuardError", "if guard did not return 'true' or 'false'");

    }

}
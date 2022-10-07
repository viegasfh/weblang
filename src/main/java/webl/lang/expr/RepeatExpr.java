package webl.lang.expr;

import webl.lang.*;

public class RepeatExpr extends Expr
{
    public Expr cond, body;

    public RepeatExpr(Expr body, Expr cond, int ppos) {
        super(ppos);
        this.cond = cond; this.body = body;
    }

    public String toString() {
        return "repeat " + body + " until " + cond + " end";
    }

    public Expr eval(Context c) throws WebLException {
        Expr e = Program.falseval;

        while (e == Program.falseval) {
            body.eval(c);

            e = cond.eval(c);
            if (!(e instanceof BooleanExpr))
                throw new WebLException(c, this, "GuardError", "repeat conditional did not return 'true' or 'false'");
        }
        return Program.nilval;
    }
}
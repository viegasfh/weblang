package webl.lang.expr;

import webl.lang.*;

public class WhileExpr extends Expr
{
    public Expr cond, body;

    public WhileExpr(Expr cond, Expr body, int ppos) {
        super(ppos);
        this.cond = cond; this.body = body;
    }

    public String toString() {
        return "while " + cond + " do " + body + " end";
    }

    public Expr eval(Context c) throws WebLException {
        Expr e;

        e = cond.eval(c);
        if (!(e instanceof BooleanExpr))
            throw new WebLException(c, this, "GuardError", "while guard did not return 'true' or 'false'");
        while (e == Program.trueval) {
            body.eval(c);

            e = cond.eval(c);
            if (!(e instanceof BooleanExpr))
                throw new WebLException(c, this, "GuardError", "while conditional did not return 'true' or 'false'");
            BasicThread.Check();    
        }
        return Program.nilval;
    }
}
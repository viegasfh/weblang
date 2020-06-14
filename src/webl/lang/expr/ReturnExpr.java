package webl.lang.expr;

import webl.lang.*;
import java.util.*;

public class ReturnExpr extends Expr
{
    public Expr body;

    public ReturnExpr(Expr body, int ppos) {
        super(ppos);
        this.body = body;
    }

    public String toString() {
        return "return " + body;
    }

    public Expr eval(Context c) throws WebLException {
        throw new WebLReturnException(c, this, body.eval(c));
    }
}
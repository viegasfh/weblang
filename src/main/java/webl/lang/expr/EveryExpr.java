package webl.lang.expr;

import webl.lang.*;
import java.util.*;

public class EveryExpr extends Expr
{
    public VarExpr loopvar;
    public Expr collection, body;

    public EveryExpr(VarExpr loopvar, Expr collection, Expr body, int ppos) {
        super(ppos);
        this.loopvar = loopvar;
        this.collection = collection;
        this.body = body;
    }

    public String toString() {
        return "every " + loopvar + " in " + collection + " do " + body + " end";
    }

    public Expr eval(Context c) throws WebLException {

        Expr col = collection.eval(c);

        if (col instanceof ContentEnumeration) {
            Expr R = Program.nilval;

            Enumeration enumeration = ((ContentEnumeration)col).getContent();
            while (enumeration.hasMoreElements()) {
                Expr x = (Expr)(enumeration.nextElement());

                loopvar.assign(c, x);
                R = body.eval(c);
                BasicThread.Check();
            }
            return R;
        } else
            throw new WebLException(c, this, "NotEnumerable", "expression does not have enumerationerable contents");

    }
}

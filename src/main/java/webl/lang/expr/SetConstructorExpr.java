package webl.lang.expr;

import java.util.*;
import webl.lang.*;

public class SetConstructorExpr extends Expr
{
    public Vector contents;

    public SetConstructorExpr(int ppos) {
        super(ppos);
        contents = new Vector();
    }

    public void addElement(Expr e) {
        contents.addElement(e);
    }

    public String toString() {
        String s = "{";

        for (int i = 0; i < contents.size(); i++) {
            s += contents.elementAt(i);
            if (i < contents.size() - 1)
                s += ", ";
        }
        return s + "}";
    }

    public Expr eval(Context c) throws WebLException {
        SetExpr s = new SetExpr();
        for (int i = 0; i < contents.size(); i++) {
            Expr e = ((Expr)contents.elementAt(i)).eval(c);
            s.DestructivePut(e);
        }
        return s;
    }
}
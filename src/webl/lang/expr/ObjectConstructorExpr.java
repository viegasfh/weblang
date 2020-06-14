package webl.lang.expr;

import java.util.*;
import webl.lang.*;

public class ObjectConstructorExpr extends Expr
{
    public Vector field, body;

    public ObjectConstructorExpr(int ppos) {
        super(ppos);
        field = new Vector();
        body = new Vector();
    }

    public boolean add(Expr fieldname, Expr body) {
        if (!field.contains(fieldname)) {
            field.addElement(fieldname);
            this.body.addElement(body);
            return true;
        } else
            return false;
    }

    public String toString() {
        int i = 0;
        String s;

        s = "[. ";
        if (field.size() > 0) {
            for(i = 0; i < field.size() - 1; i ++)
                s += field.elementAt(i) + " = " + body.elementAt(i) + ", ";
            s += field.elementAt(i) + " = " + body.elementAt(i);
        }
        return s + " .]";
    }

    public Expr eval(Context c) throws WebLException {
        ObjectExpr obj = new ObjectExpr();
        Expr id;
        Expr b;

        for (int i = 0; i < field.size(); i++) {
            id = (Expr)field.elementAt(i);
            b = (Expr)body.elementAt(i);
            obj.def(id, b.eval(c));
        }
        return obj;
    }
}
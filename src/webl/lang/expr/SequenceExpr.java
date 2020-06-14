package webl.lang.expr;

import webl.lang.*;
import java.util.*;

public class SequenceExpr extends Expr
{
    public Vector E = new Vector();

    public SequenceExpr(int ppos) {
        super(ppos);
    }

    public void append(Expr e) {
        if (e != null)
            E.addElement(e);
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("(");
        
        for (int i = 0; i < E.size()-1; i++)
            buf.append(E.elementAt(i)).append("; ");
        buf.append(E.elementAt(E.size() - 1)).append(")");
        return buf.toString();
    }

    public Expr eval(Context c) throws WebLException {
        Expr R = Program.nilval;
        int i = 0, count = E.size();
        
        while (i < count) 
            R = ((Expr)(E.elementAt(i++))).eval(c);
        return R;
    }
}
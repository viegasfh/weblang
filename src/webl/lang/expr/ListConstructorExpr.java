package webl.lang.expr;

import java.util.*;
import webl.lang.*;

public class ListConstructorExpr extends Expr
{
    public Vector contents;
    public boolean constructinparallel;
    
    public ListConstructorExpr(int ppos, boolean constructinparallel) {
        super(ppos);
        contents = new Vector();
        this.constructinparallel = constructinparallel;
    }

    public void appendElement(Expr e) {
        contents.addElement(e);
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        
        if (constructinparallel)
            s.append("[|");
        else
            s.append("[");

        for (int i = 0; i < contents.size(); i++) {
            s.append(contents.elementAt(i));
            if (i < contents.size() - 1)
                s.append(", ");
        }
        if (constructinparallel)
            s.append("|]");
        else
            s.append("]");
            
        return s.toString();
    }

    public Expr eval(Context c) throws WebLException {
        if (constructinparallel)
            return WebLThread.ParallelExecution(c, this, contents);
        else {
            ListExpr l = new ListExpr();
            for (int i = 0; i < contents.size(); i++) {
                Expr e = ((Expr)contents.elementAt(i)).eval(c);
                l = l.Append(e);
            }
            return l;
        }
    }
}
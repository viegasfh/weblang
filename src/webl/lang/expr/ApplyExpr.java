package webl.lang.expr;

import java.util.*;
import webl.lang.builtins.*;
import webl.lang.*;

public class ApplyExpr extends Expr
{
    public Vector args = new Vector();
    public Expr lh;

    public ApplyExpr(Expr lh, int ppos) {
        super(ppos);
        this.lh = lh;
    }

    public void addArg(Expr expr) {
        args.addElement(expr);
    }

    public String toString() {
        String s = lh.toString() + "(";

        for (int i = 0; i < args.size(); i++) {
            s += (Expr)args.elementAt(i);
            if (i < args.size() - 1)
                s += ", ";
        }
        return s + ")";
    }

    public Expr eval(Context c) throws WebLException {
        BasicThread.Check();
        if (lh instanceof IndexExpr) {
            IndexExpr n = (IndexExpr)lh;
            Expr o = n.obj.eval(c);
            Expr k = n.index.eval(c);
            if (o instanceof ObjectExpr) {
                ObjectExpr obj = (ObjectExpr)o;
                Expr m = obj.get(k);
                if (m == null)
                    throw new WebLException(c, this, "NoSuchField", "object does not have a field called " + k);

                if (m instanceof AbstractMethExpr) {
                    AbstractMethExpr meth = (AbstractMethExpr)m;
                    return meth.Apply(c, (Expr)obj, args, this);
                } else if (m instanceof AbstractFunExpr)
                    return ((AbstractFunExpr)m).Apply(c, args, this);
                else
                    throw new WebLException(c, this, "NotAFunctionOrMethod", "not a function or a method");
            } else
                throw new WebLException(c, this, "NotAnObject", "not an object");
        } else {    // must be a normal function
            Expr fun = lh.eval(c);
            if (fun instanceof AbstractFunExpr) {
                return ((AbstractFunExpr)fun).Apply(c, args, this);
            } else
                throw new WebLException(c, this, "NotAFunctionOrMethod", "not a function");
        }
    }

}
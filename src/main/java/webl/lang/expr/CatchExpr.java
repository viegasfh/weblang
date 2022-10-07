package webl.lang.expr;

import webl.lang.*;
import java.util.*;

public class CatchExpr extends Expr
{
    public Expr body;               // main body
    public Vector conds, bodies;    // conditions and corresponding actions
    public VarExpr evar;            // exception object is bound to this

    public CatchExpr(Expr body, VarExpr evar, Vector conds, Vector bodies, int ppos) {
        super(ppos);
        this.body = body;
        this.evar = evar;
        this.conds = conds;
        this.bodies = bodies;
    }

    public String toString() {
        String s = "try " + body + " catch " + evar;
        
        for (int i = 0; i < conds.size(); i++) {
            s += " on " + conds.elementAt(i).toString() + " do " +
                bodies.elementAt(i).toString();
        }
        return s + " end";
    }

    public Expr eval(Context c) throws WebLException {
        Expr val;
        try {
            val = body.eval(c);
        } catch (WebLException e) {
            evar.assign(c, e.MakeObject());
            
            // check each of the conditions in turn
            int i = 0;
            while (i < conds.size()) {
                Expr T = ((Expr)(conds.elementAt(i))).eval(c);
                
                if (T instanceof BooleanExpr) {
                    if (T == Program.trueval) 
                        return ((Expr)(bodies.elementAt(i))).eval(c);
                } else
                    throw new WebLException(c, this, "GuardError", "on expression did not return true or false");
                i++;
            }
            throw e;        // reraise the exception
        }
        return val;
    }

}



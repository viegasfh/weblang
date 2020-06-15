package webl.lang.expr;

import webl.lang.*;
import java.util.*;

abstract public class AbstractMethExpr extends ValueExpr
{
    public AbstractMethExpr(int ppos) {
        super(ppos);
    }   
   
    public AbstractMethExpr() {
        super(-1);
    }   
    
    public String getTypeName() {
        return "meth";
    }
    
    abstract public String toString();
    
    abstract public Expr Apply(Context c, Expr self, Vector cargs, Expr callsite) throws WebLException;

    protected void CheckArgCount(Context c, Vector args, Expr callsite, int count) throws WebLException {
        if (args.size() != count)
            throw new WebLException(c, callsite, "ArgumentError", "wrong number of arguments passed to " + this + " function");
       
    }
    
    protected String StringArg(Context c, Vector args, Expr callsite, int param) throws WebLException {
        Expr r = ((Expr)(args.elementAt(param))).eval(c);
        if (r instanceof StringExpr)
            return ((StringExpr)r).val();
        else
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a string as argument " + param);
    }    
}

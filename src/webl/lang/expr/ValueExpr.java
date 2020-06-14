package webl.lang.expr;
import webl.lang.*;

// base class for nodes that are results of expression evaluations
abstract public class ValueExpr extends Expr
{
    final public Expr eval(Context c) {   // valuenodes evaluate to themselves
        return this;
    }
    
    public ValueExpr(int ppos) { super(ppos); }
    
    abstract public String getTypeName();
}
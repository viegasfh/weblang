package webl.lang.expr;

import webl.lang.*;

public abstract class Expr
{
    public int ppos;
    
    abstract public Expr eval(Context c) throws WebLException;
    
    public Expr(int ppos) {
        this.ppos = ppos;
    }
    
    /**
    @return String value of expr for print function
    */
    public String print() {
        return toString();
    }
}
package webl.lang.expr;

import webl.lang.*;

public class VarExpr extends Expr
{
    public String  name;           // variable name
    public int     varOffset;      // in context stack
    public int     levelsUp;       // how many levels up in context stack
    public Module  mod;            // if reference to designator in another module
    
    public VarExpr(String name, int varOffset, int levelsUp) {
        super(-1);
        this.name = name;
        this.varOffset = varOffset;
        this.levelsUp = levelsUp;
        this.mod = null;
    }
    
    public VarExpr(String name, Module mod, int varOffset) {
        super(-1);
        this.name = name;
        this.varOffset = varOffset;
        this.levelsUp = 0;
        this.mod = mod;        
    }
    
    public String toString() {
        return name; // + "-" + levelsUp + "_" + varOffset;
    }
    
    public Expr eval(Context c) throws WebLException {
        return lookup(c);
    }    
    
    public Expr lookup(Context c) {
        if (mod != null)
            return mod.context.binding[varOffset];
        else {
            int lev = levelsUp;
            while (lev-- > 0) c = c.next;
            return c.binding[varOffset];
        }
    }

    public void assign(Context c, Expr val) {
        if (mod != null)
            mod.context.binding[varOffset] = val;
        else {
            int lev = levelsUp;
            while (lev-- > 0) c = c.next;
            c.binding[varOffset] = val;        
        }
    }    
}

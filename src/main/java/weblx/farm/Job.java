package weblx.farm;

import webl.lang.*;
import webl.lang.expr.*;

public class Job
{
    Job next;
    private Context c;
    private Expr e;
    
    public Job(Context c, Expr e) {
        this.c = c;
        this.e = e;
    }
    
    public Context getContext() {
        return c;
    }
    
    public Expr getExpr() {
        return e;
    }
    
}
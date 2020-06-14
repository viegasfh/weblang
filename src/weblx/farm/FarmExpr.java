package weblx.farm;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class FarmExpr extends ObjectExpr
{
    private static PerformMethod    performMethod = new PerformMethod();
    private static IdleMethod       idleMethod = new IdleMethod();
    private static StopMethod       stopMethod = new StopMethod();
    
    private int                     nofWorkers;
    private WorkerThread[]          worker;
    private JobQueue                Q = new JobQueue();
    
    public FarmExpr(int nofWorkers) {
        this.nofWorkers = nofWorkers;
        worker = new WorkerThread[nofWorkers];
        for(int i = 0; i < nofWorkers; i++)
            worker[i] = new WorkerThread(Q);
        
        def("Perform", performMethod);
        def("Idle", idleMethod);
        def("Stop", stopMethod);
    }

    public void perform(Context c, Expr e) {
        Q.put(new Job(c, e));
    }
    
    public boolean idle() {
        return (Q.length() == 0 && Q.getNoOfWaiters() == nofWorkers);
    }
    
    public void stop() {
        for(int i = 0; i < nofWorkers; i++)
            worker[i].pleaseStop();
        Q.close();
    }
    
    protected void finalize() throws Throwable {
    }
}

class PerformMethod extends AbstractMethExpr  
{

    public PerformMethod() {
        super(-1);
    }

    public String toString() {
        return "<Perform>";
    }
    
    public Expr Apply(Context c, Expr self, Vector cargs, Expr callsite) throws WebLException {
        if (cargs.size() != 1)
            throw new WebLException(c, callsite, "ArgumentError", "number of formals and arguments do not match");

        if (self instanceof FarmExpr) {
            if ((Expr)cargs.firstElement() instanceof ApplyExpr) {                
                ApplyExpr e = (ApplyExpr)cargs.firstElement();
                Expr fn = e.lh.eval(c);
                if (!(fn instanceof AbstractFunExpr))
                    throw new WebLException(c, callsite, "ArgumentError", "argument is not an application of a function");
                    
                ApplyExpr expr = new ApplyExpr(fn, e.ppos);
                
                for (int i = 0; i < e.args.size(); i++) {
                    expr.addArg(((Expr)e.args.elementAt(i)).eval(c));
                }
                Context dc = new Context(c.scope.machine.universe, null, new Scope(c.scope.machine), callsite);
                ((FarmExpr)self).perform(dc, expr);
                return Program.nilval;
            } else
                throw new WebLException(c, callsite, "ArgumentError", "argument is not a function application");            

        }
        
        throw new WebLException(c, callsite, "MethodError", "self is not a farm object");
    }
}

class IdleMethod extends AbstractMethExpr  
{

    public IdleMethod() {
        super(-1);
    }

    public String toString() {
        return "<Idle>";
    }
    
    public Expr Apply(Context c, Expr self, Vector cargs, Expr callsite) throws WebLException {
        if (cargs.size() != 0)
            throw new WebLException(c, callsite, "ArgumentError", "number of formals and arguments do not match");

        if (self instanceof FarmExpr) {
            boolean idle = ((FarmExpr)self).idle();
            if (idle)
                return Program.trueval;
            else
                return Program.falseval;
        }
        
        throw new WebLException(c, callsite, "MethodError", "self is not a farm object");
    }
}

class StopMethod extends AbstractMethExpr  
{

    public StopMethod() {
        super(-1);
    }

    public String toString() {
        return "<Stop>";
    }
    
    public Expr Apply(Context c, Expr self, Vector cargs, Expr callsite) throws WebLException {
        if (cargs.size() != 0)
            throw new WebLException(c, callsite, "ArgumentError", "number of formals and arguments do not match");

        if (self instanceof FarmExpr) {
            ((FarmExpr)self).stop();
            return Program.nilval;
        }
        throw new WebLException(c, callsite, "MethodError", "self is not a farm object");
    }
}
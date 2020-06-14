package weblx.farm;

import webl.lang.*;
import webl.lang.expr.*;
import webl.util.*;

public class WorkerThread extends BasicThread
{
    JobQueue Q;
    
    static long start;
    static int jobs = 0;
    
    static {
        start = System.currentTimeMillis();     
    }
    
    public WorkerThread(JobQueue Q) {
        this.Q = Q;
        start();
    }
    
    public Expr Execute() {
        while(true) {
            Job j = Q.get();
            if (j == null) break;
            
            try {
                j.getExpr().eval(j.getContext());
                synchronized (Q) {
                    jobs++;
                }
                if (jobs % 25 == 0) {
                    long elapse = (System.currentTimeMillis() - start) / 1000;
                    double js = jobs * 1.0 / elapse;
                    Log.debugln("[Farm performance sec=" + elapse + " qsize=" + Q.Size() + " waiters=" + Q.getNoOfWaiters() + " jobs=" + jobs + " jobs/s=" + js + "]");
                }
            } catch (WebLException e) {
                Log.debugln("[Worker exception " + e.report() + "]");
            } catch(Exception e) {
                Log.debugln("[Worker Exception " + e + "]");
                e.printStackTrace();
            }
            j = null;       // for GC
        }
        return Program.nilval;
    }
}
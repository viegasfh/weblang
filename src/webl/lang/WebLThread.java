package webl.lang;

import webl.lang.*;
import webl.lang.expr.*;
import webl.util.Log;
import java.util.*;

public class WebLThread extends BasicThread
{
    private Expr            body;               // body to be executed
    private Context         context;
    
    public WebLThread(Context context, Expr body) {
        super();
        this.context = context;
        this.body = body;
        start();
    }
    
    public Expr Execute() throws Exception {
        return body.eval(context);
    }    
    
//
// Combinator implementations
//

    public static Expr CondCombinator(Context c, Expr callsite, Expr x, Expr y) throws WebLException {
        Expr result;
        
        try {
            result = x.eval(c);
        } catch (WebLException e) {
            result = y.eval(c);
        }
        return result;
    }
    
    public static Expr BarCombinator(Context c, Expr callsite, Expr x, Expr y) throws WebLException {
        WebLThread x0 = new WebLThread(c, x);
        WebLThread y0 = new WebLThread(c, y);
        
        Thread T = Thread.currentThread();
        for(;;) {
            try {
                synchronized (T) {
                    T.wait(5000);
                }
            } catch (InterruptedException e) {
                x0.pleaseStop(); y0.pleaseStop();
                BasicThread.Interrupt();
            }
            BasicThread.Check();                        // this thread might have been stopped
            if (x0.getState() == SUCCESS) {
                x0.pleaseStop(); y0.pleaseStop();
                return x0.getResult();
            }
            if (y0.getState() == SUCCESS) {
                x0.pleaseStop(); y0.pleaseStop();
                return y0.getResult();
            }
            if (x0.getState() == FAILED && y0.getState() == FAILED) {
                x0.pleaseStop(); y0.pleaseStop();
                return x0.getResult();
            }
        }
    }
    
    public static Expr TimeoutCombinator(Context c, Expr callsite, long ms, Expr x) throws WebLException {
        Thread T = Thread.currentThread();
        
        WebLThread x0 = new WebLThread(c, x);
        try {
            synchronized (T) {
                T.wait(ms);
            }
        } catch (InterruptedException e) {
            x0.pleaseStop();
            BasicThread.Interrupt();
        }
        
        BasicThread.Check();                // thread might have been stopped
        if (x0.getState() == RUNNING) {
            x0.pleaseStop();
            throw new WebLException(c, callsite, "Timeout", "timeout");
        } else {
            x0.pleaseStop();
            return x0.getResult();
        }
    }

    private static void pleaseStop(Vector W) {
        int count = W.size();
        for(int i = 0; i < count; i++) 
           ((WebLThread)W.elementAt(i)).pleaseStop();
    }
    
    public static Expr ParallelExecution(Context c, Expr callsite, Vector E) throws WebLException {
        int count = E.size();
        
        Vector W = new Vector(count);
        for(int i = 0; i < count; i++) 
            W.addElement(new WebLThread(c, ((Expr)E.elementAt(i))));
        
        Thread T = Thread.currentThread();
        for(;;) {
            try {
                synchronized (T) {
                    T.wait(5000);
                }
            } catch (InterruptedException e) {
                pleaseStop(W);
                BasicThread.Interrupt();
            }
            BasicThread.Check();                        // this thread might have been stopped
            
            int successcount = 0;
            for(int i = 0; i < count; i++) {
                WebLThread w = (WebLThread)W.elementAt(i);
                if (w.getState() == FAILED) {
                    pleaseStop(W);
                    return w.getResult();
                } else if (w.getState() == SUCCESS) {
                    successcount++;
                }
            }
            if (successcount == count) {
                pleaseStop(W);
                
                ListExpr list = new ListExpr();
                for(int i = 0; i < count; i++) {
                    WebLThread w = (WebLThread)W.elementAt(i);
                    list = list.Append(w.getResult());
                }
               return list;
            }
        }        
    }    
}

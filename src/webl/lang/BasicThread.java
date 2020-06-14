package webl.lang;

import webl.lang.*;
import webl.lang.expr.*;
import webl.util.Log;
import java.util.*;

abstract public class BasicThread extends Thread
{
    private static int              idgen=0;
    private int                     id;         // number of this thread
    
    private Thread          parent;             // parent thread to notify when this thread finishes
    private BasicThread     dsc, next;          // children and sibling pointers
    
    private int             state;              // current state of the thread (running, success, failed)
    public final static int RUNNING = 1, SUCCESS = 2, FAILED = 3;
    
    private Expr            resultExpr;         // result of executing the body in context
    private WebLException   resultException;    // exception resulting from execution
    
    private boolean pleasestopnow = false;      // request to self to stop
    
    public BasicThread() {
        state = RUNNING;
        id = idgen++;
        
        Thread T = Thread.currentThread();
        if (T instanceof BasicThread) 
            ((BasicThread)T).insertChild(this);
        else
            parent = T;
    }
    
    public abstract Expr Execute() throws Exception;
    
    public void run() {
        try {
            Check();
            resultExpr = Execute();
            state = SUCCESS;
        } catch (WebLInterrupt e) {
            resultException = new WebLException(null, null, "ThreadStopped", "Thread was stopped");
            state = FAILED;
        } catch (WebLException e) {
            resultException = e;
            state = FAILED;
        } catch (Exception e) {
            resultException = new WebLException(null, null, "UnknownException", "Unknown exception caught in webl thread");
            state = FAILED;
        }
        
        if (parent != null && !pleasestopnow)
            synchronized(parent) {
                parent.notify();
            }
    }    
    
    final public synchronized int getState() {
        return state;
    }
    
    final public synchronized Expr getResult() throws WebLException {
        if (state == SUCCESS)
            return resultExpr;
        else if (state == FAILED)
            throw resultException;
        else
            return null;
    }
    
    private void stopChildren() {
        // recursively stop all children threads
        BasicThread p = dsc;
        while (p != null) {
            p.stopChildren();
            p = p.next;
        }
    }
    
    public synchronized void pleaseStop() {
        stopChildren();
        pleasestopnow = true;
        if (parent != null && parent instanceof BasicThread)        // for GC
            ((BasicThread)parent).removeChild(this);
    }
    
    private synchronized void insertChild(BasicThread t) {
        t.parent = this;
        t.next = dsc;
        dsc = t;
    }
    
    private synchronized void removeChild(BasicThread t) {
        BasicThread p = dsc;
        BasicThread prev = null;
        while (p != null && p != t) {
            prev = p;
            p = p.next;
        }
        if (p == t) {       // found
            if (prev == null) 
                dsc = t.next;
            else 
                prev.next = t.next;
        }
    }
    
    public static void Interrupt() {
        throw new WebLInterrupt();
    }
    
    public static void Check() {
        Thread T = Thread.currentThread();
        if (T instanceof BasicThread && ((BasicThread)T).pleasestopnow) 
            Interrupt();
    }
    
}


class WebLInterrupt extends RuntimeException {
}

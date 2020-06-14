package webl.lang;

// this class is not used at the moment.

import webl.lang.expr.*;

public class WebLThreadFarm
{
    int                 noworkers;
    WorkerThread[]      worker;
    JobQueue            Q = new JobQueue();
    boolean             halt = false;
    
    public WebLThreadFarm(int noworkers) {
        this.noworkers = noworkers;
        worker = new WorkerThread[noworkers];
        for(int i = 0; i < noworkers; i++)
            worker[i] = new WorkerThread(this);
    }
    
    synchronized public void perform(Context c, Expr e) {
        Job j = new Job(c, e);
        Q.put(j);
    }
    
    synchronized public Job getJob() {
        if (halt)
            return null;
            
        return Q.get();
    }
    
    synchronized public void stop() {
        halt = true;
    }
    
}

class WorkerThread extends BasicThread
{
    WebLThreadFarm farm;
    
    public WorkerThread(WebLThreadFarm farm) {
        this.farm = farm;
        start();
    }
    
    public Expr Execute() {
        while(true) {
            Job j = farm.getJob();
            if (j == null) break;
            
            try {
                j.e.eval(j.c);
            } catch(Exception e) {
            }
        }
        return Program.nilval;
    }
}

class JobQueue
{
    int length = 0;
    Job head = null, tail = null;
    
    synchronized int length() {
        return length;
    }
    
    synchronized public void put(Job j) {
        j.next = null;
        
        if (tail == null) 
            head = tail = j;
        else {
            tail.next = j;
            tail = j;
        }
        length++;
        this.notify();
    }
    
    synchronized public Job get() {
        try {
            while(length == 0) 
                this.wait();
        } catch (InterruptedException e) {
            return null;
        }
        Job j = head;
        
        head = head.next;
        if (head == null)
            tail = null;
        length--;
        return j;
    }
}

class Job
{
    Job next;
    Context c;
    Expr e;
    
    public Job(Context c, Expr e) {
        this.c = c;
        this.e = e;
    }
}


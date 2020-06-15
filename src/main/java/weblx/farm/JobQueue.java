package weblx.farm;

public class JobQueue
{
    int length = 0;
    int waiters = 0;
    Job head = null, tail = null;
    boolean closed = false;
    
    public synchronized int length() {
        return length;
    }
    
    public synchronized int getNoOfWaiters() {
        return waiters;
    }
    
    public synchronized int Size() {
        return length;
    }
    
    public synchronized void put(Job j) {
        j.next = null;
        
        if (tail == null) 
            head = tail = j;
        else {
            tail.next = j;
            tail = j;
        }
        length++;
//        if (length == 1)
            this.notify();
    }
    
    public synchronized Job get() {
        if (closed)
            return null;
            
        try {
            while(length == 0) {
                waiters++;
                this.wait();
                waiters--;
                if (closed)
                    return null;
            }
        } catch (InterruptedException e) {
            return null;
        }
        Job j = head;
        
        head = head.next;
        if (head == null)
            tail = null;
            
        j.next = null;
        length--;
        return j;
    }
    
    public synchronized void close() {
        closed = true;
        this.notifyAll();
    }
}
package webl.util;

public class PriorityQueue
{
    private PQCell head;
    private int count = 0;
    
    public PriorityQueue() {
        head = new PQCell(null, Long.MAX_VALUE);
    }
    
    synchronized public int size() {
        return count;
    }
    
    synchronized public boolean empty() {
        return count == 0;
    }
    
    // lower priority # is on "top" of the stack, equal priority #'s give preference to objects
    // already in the queue.
    synchronized public void put(Object obj, long priority) {
        PQCell p;
        p = head.next;
        while (priority >= p.priority) p = p.next;
        insertPQCellBefore(p, new PQCell(obj, priority));
        count++;
    }
    
    synchronized public Object get() {
        if (count == 0)
            return null;
        else {
            Object res = head.next.obj;
            removePQCell(head.next);
            count--;
            return res;
        }
    }
    
    synchronized public boolean remove(Object obj) {
        PQCell p = locate(obj);
        if (p == null)
            return false;
        else {
            removePQCell(p);
            return true;
        }
    }
    
    synchronized public boolean contains(Object obj) {
        return locate(obj) != null;
    }
    
    private PQCell locate(Object obj) {
        PQCell p = head.next;
        while (p != head) {
            if (p.obj == obj) return p;
            p = p.next;
        }
        return null;
    }
    
    private void removePQCell(PQCell x) {
        PQCell prev = x.prev;
        PQCell next = x.next;
        
        prev.next = next; next.prev = prev;
        x.next = x; x.prev = x;
    }
    
    private void insertPQCellBefore(PQCell x, PQCell n) {
        n.prev = x.prev; n.next = x;
        x.prev.next = n; x.prev = n;
    }    
}

class PQCell
{
    PQCell prev, next;
    long priority;
    Object obj;
    
    public PQCell(Object obj, long priority) {
        this.obj = obj;
        this.priority = priority;
        next = prev = this;
    }
        
}

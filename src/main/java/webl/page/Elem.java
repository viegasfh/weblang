package webl.page;

abstract public class Elem
{
    public Elem      prev, next;     // doubly linked list
    
    public long     sno;            // search sequence number
    public int      charwidth;      // number of characters in this elem
    
    public Elem() {
        sno = charwidth = 0;
        next = prev = this;
    }
    
    final boolean Valid() {
        return (next != this && prev != this);
    }
}



package webl.page;

public class Tag extends Elem
{
    private int     ref;            // reference count
    private Piece   owner;          // owner piece of the HTML tag marker
    
    public Tag(Piece owner) {
        ref = 0;
        this.owner = owner;
    }
    
    synchronized public boolean Empty() {
        return owner == null;
    }
    
    synchronized public Piece getOwner() {
        return owner;
    }
    
    synchronized public void setOwner(Piece p) {
        owner = p;
    }
    
    // add reference count
    synchronized final public void addRef() {
        ref++;
    }
    
    // decrease reference count
    synchronized final public void releaseRef(Page p) {
        ref--;
        if (ref == 0)
            p.ScheduleCleanup();
        else if (ref < 0)
            throw new Error("Tag reference count went below zero");
    }    
    
    synchronized final public int getRefCount() {
        return ref;
    }
}
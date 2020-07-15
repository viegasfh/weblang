package webl.page;

import webl.lang.*;
import webl.lang.expr.*;
import webl.util.*;
import java.util.*;
import java.util.regex.*;

public class PieceSet extends ValueExpr implements ContentEnumeration
{
    public Cell     head;                   // dummy head of the doubly linked ring of cells
    public Page     page;                   // page this piece-set belongs to
    
    private int     cellcount = 0;          // # of cells the piece list contains (length)

    public PieceSet(Page p) {
        super(-1);
        head = new Cell(null);
        page = p;
    }
    
    public String getTypeName() {
        return "pieceset";
    }
    
    // pieceset invariants are not checked
    final public void append(Piece p) {
        insertBefore(head, p);
    }
    
    // pieceset invariants are not checked
    final public void prepend(Piece p) {
        insertAfter(head, p);
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        Cell x = head.next;
        while (x != head) {
            
            buf.append(x.pce.toString());
            x = x.next;
            if (x != head)
                buf.append(", ");
        }
        buf.append("}");
        return buf.toString();
    }
    
    // for enumerating all the pieces that belong to the pieceset
    public Enumeration getContent() {
        return new PieceSetEnumeration(this);
    }
    
    Cell lastpos = null;
    
    // pieceset invariants are enforced
    final public void insert(Piece p) throws TypeCheckException {
        boolean hit;
        int count=0;
        
        CheckCompatible(page, p);
        
        Cell c;
        if (lastpos != null && Piece.inorder(lastpos.pce, p)) {
            c = lastpos;
            hit = true;
        } else {
            c = head.next;
            hit = false;
        }
        
        while (c != head && Piece.inorder(c.pce, p)) {
            if (c.pce == p) return;         // identical pieces
            c = c.next;
            count++;
        }
        insertBefore(c, p);
        lastpos = c.prev;
    }
    
    // insert according to the end-point sorting order, invariants not checked
    final private void insertRev(Piece p) {
        Cell c = head.next;
        
        while (c != head && Piece.inrevorder(c.pce, p)) c = c.next;
        insertBefore(c, p);
    }    
    
    final public int getSize() {
        return cellcount;
    }
    
    final public void remove(Piece p) throws TypeCheckException {
        CheckCompatible(page, p);
            
        Cell x = head.next;
        while (x != head && x.pce != p) x = x.next;
        if (x == head)
            throw new Error("no such element");

        Cell prev = x.prev;
        Cell next = x.next;
        
        prev.next = next; next.prev = prev;
        x.next = x; x.prev = x;
    }
    
    //
    // private stuff
    //
    
    final protected void insertAfter(Cell x, Piece p) {
        Cell n = new Cell(p);
        n.prev = x; n.next = x.next;
        x.next.prev = n; x.next = n;
        cellcount++;
    }
    
    final protected void insertBefore(Cell x, Piece p) {
        insertAfter(x.prev, p);
    }
    
    //
    // operators
    //
    
    public static PieceSet make(Piece p) {
        PieceSet R = new PieceSet(p.page);
        R.append(p);
        return R;
    }
    
    public static PieceSet castExpr(Expr x) {
        if (x instanceof PieceSet)
            return (PieceSet)x;
        else if (x instanceof Piece) 
            return make((Piece)x);
        else
            return null;
    }

    public final boolean member(Piece p) {
        if (p.page != this.page) return false;
        Cell x = head.next;
        while (x != head && x.pce != p) x = x.next;
        return x != head;
    }
    
    public static PieceSet OpElem(PieceSet x, String name) {
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.next;
        while (p != x.head) {
            if (p.pce.name.equals(name)) R.append(p.pce);
            p = p.next;
        }
        return R;
    }

    public static PieceSet OpElemByClass(PieceSet x, String c) {
        PieceSet R = new PieceSet(x.page);
        String[] classes = c.split(" ");
        Cell p = x.head.next;
        boolean contains = true;
        Pattern pattern = null;
        Matcher matcher = null;

        while (p != x.head) {
            String attr = p.pce.getAttr("class");

            if (attr != null) {
                for (int i = 0; i < classes.length; i++) {
                    pattern = Pattern.compile("\\b" + classes[i] + "\\b");
                    matcher = pattern.matcher(attr);

                    if (matcher.find()) {
                        contains &= true;
                    }
                    else {
                        contains &= false;
                    }
                }
            } else
                contains = false;

            if (contains)
              R.append(p.pce);
            
            p = p.next;
            contains = true;
        }
        return R;
    }

    // gets an element by id from the entire page
    public static Piece OpElemById(PieceSet x, String pieceId) {
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.next;
        while (p != x.head) {
            if (p.pce.getAttr("id") != null && p.pce.getAttr("id").equals(pieceId)) 
              return p.pce;
            p = p.next;
        }
        return null;
    }   

    public PieceSet OpClone() {
        PieceSet R = new PieceSet(this.page);
        Cell p = this.head.next;
        while (p != this.head) {
            R.append(p.pce);
            p = p.next;
        }
        return R;
    }
    
    public static Piece OpIndex(PieceSet x, int n) throws IndexOutOfBoundsException {
        if (n < 0 || n >= x.cellcount) 
            throw new IndexOutOfBoundsException();
            
        Cell p = x.head.next;
        int i = 0;
        while (p != x.head && (i++ < n)) p = p.next;
        if (p == x.head)
            throw new IndexOutOfBoundsException();
        else
            return p.pce;
    }
    
    public static PieceSet OpSelect(PieceSet x, int from, int to) throws IndexOutOfBoundsException {
        if (from < 0 || from >= x.cellcount || to < 0 || to > x.cellcount || to < from) 
            throw new IndexOutOfBoundsException();
            
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.next;
        int i = 0;
        while (i++ < from) p = p.next;
        
        while (i++ <= to) {
            R.append(p.pce);
            p = p.next;
        }
        return R;
    }
    
    public static PieceSet OpUnion(PieceSet x, PieceSet y) throws TypeCheckException {
        CheckCompatible(x, y);
        
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.next;
        Cell q = y.head.next;
        while (p != x.head || q != y.head) {
            if (p == x.head) {
                while (q != y.head) {
                    R.append(q.pce);
                    q = q.next;
                }
            } else if (q == y.head) {
                while (p != x.head) {
                    R.append(p.pce);
                    p = p.next;
                }
            } else if (Piece.equal(p.pce, q.pce)) {
                R.append(p.pce);
                p = p.next;
                q = q.next;
            } else if (Piece.inorder(p.pce, q.pce)) {
                R.append(p.pce);
                p = p.next;
            } else {
                R.append(q.pce);
                q = q.next;
            }
        }
        return R;
    }
    
    public static boolean OpEqual(PieceSet x, PieceSet y) throws TypeCheckException {
        CheckCompatible(x, y);
        
        if (x.cellcount != y.cellcount) return false;
        Cell p = x.head.next;
        Cell q = y.head.next;
        while (p != x.head) {
            if (!Piece.equal(p.pce, q.pce)) return false;
            p = p.next;
            q = q.next;
        }
        return q == y.head;
    }
    
    /*  "*" operator */
    public static PieceSet OpIntersect(PieceSet x, PieceSet y, boolean invert) throws TypeCheckException {
        CheckCompatible(x, y);
        
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.next;
        Cell q = y.head.next;
        
        Cell pend = x.head;
        Cell qend = y.head;
        
        while (p != pend) {
            if (q == qend) {
                if (invert) R.append(p.pce);
                p = p.next;
            } else if (Piece.cbefore(p.pce, q.pce)) {
                if (invert) R.append(p.pce);
                p = p.next;
            } else if (Piece.cbefore(q.pce, p.pce)) {
                q = q.next;
            } else {
                boolean found = false;
                Cell r = q;
                while (r != qend && !Piece.cafter(r.pce, p.pce)) {
                    if (Piece.equal(p.pce, r.pce)) {
                        found = true;
                        break;
                    }
                    r = r.next;
                }
                if (found ^ invert) R.append(p.pce);
                p = p.next;
            }
        }
        return R;
    }    
  
    public static PieceSet OpMinus(PieceSet x, PieceSet y) throws TypeCheckException {
        CheckCompatible(x, y);
        
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.next;
        Cell q = y.head.next;
        
        Cell pend = x.head;
        Cell qend = y.head;
        
        while (p != pend) {
            if (q == qend) {
                R.append(p.pce);
                p = p.next;
            } else if (Piece.before(p.pce, q.pce)) {
                R.append(p.pce);
                p = p.next;
            } else if (Piece.before(q.pce, p.pce)) {
                q = q.next;
            } else if (Piece.equal(p.pce, q.pce)) {
                p = p.next;
            } else {
                R.append(p.pce);
            }
        }        
        return R;
    }   
    
  public static PieceSet OpContain(PieceSet x, PieceSet y, boolean invert) throws TypeCheckException {
        CheckCompatible(x, y);
        
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.next;
        Cell q = y.head.next;
        
        Cell pend = x.head;
        Cell qend = y.head;
        
        while (p != pend) {
            if (q == qend) {
                if (invert) R.append(p.pce);
                p = p.next;
            } else if (Piece.cbefore(p.pce, q.pce)) {
                if (invert) R.append(p.pce);
                p = p.next;
            } else if (Piece.cbefore(q.pce, p.pce)) {
                q = q.next;
            } else {
                boolean found = false;
                Cell r = q;
                while (r != qend && !Piece.cafter(r.pce, p.pce)) {
                    if (Piece.contain(p.pce, r.pce)) {
                        found = true;
                        break;
                    }
                    r = r.next;
                }
                if (found ^ invert) R.append(p.pce);
                p = p.next;
            }
        }
        return R;
    }
    
    public static PieceSet OpInside(PieceSet x, PieceSet y, boolean invert) throws TypeCheckException {
        CheckCompatible(x, y);
        
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.next;
        Cell q = y.head.next;
        
        Cell pend = x.head;
        Cell qend = y.head;
        
        while (p != pend) {
            if (q == qend) {
                if (invert) R.append(p.pce);
                p = p.next;
            } else if (Piece.cbefore(p.pce, q.pce)) {
                if (invert) R.append(p.pce);
                p = p.next;
            } else if (Piece.cbefore(q.pce, p.pce)) {
                q = q.next;
            } else {
                boolean found = false;
                Cell r = q;
                while (r != qend && !Piece.cafter(r.pce, p.pce)) {
                    if (Piece.in(p.pce, r.pce)) {
                        found = true;
                        break;
                    }
                    r = r.next;
                }
                if (found ^ invert) R.append(p.pce);
                p = p.next;
            }
        }
        return R;
    }
    
    public static PieceSet OpOverlap(PieceSet x, PieceSet y, boolean invert) throws TypeCheckException {
        CheckCompatible(x, y);
        
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.next;
        Cell q = y.head.next;
        
        Cell pend = x.head;
        Cell qend = y.head;
        
        while (p != pend) {
            if (q == qend) {
                if (invert) R.append(p.pce);
                p = p.next;
            } else if (Piece.cbefore(p.pce, q.pce)) {
                if (invert) R.append(p.pce);
                p = p.next;
            } else if (Piece.cbefore(q.pce, p.pce)) {
                q = q.next;
            } else {
                boolean found = false;
                Cell r = q;
                while (r != qend && !Piece.cafter(r.pce, p.pce)) {
                    if (Piece.overlap(p.pce, r.pce)) {
                        found = true;
                        break;
                    }
                    r = r.next;
                }
                if (found ^ invert) R.append(p.pce);
                p = p.next;
            }
        }
        return R;
    }        
    
    public static PieceSet OpBefore(PieceSet x, PieceSet y, boolean invert) throws TypeCheckException {
        CheckCompatible(x, y);
        
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.prev;
        Cell q = y.head.prev;
        while (p != x.head) {
            if (q == y.head) {
                if (invert) R.prepend(p.pce);
                p = p.prev;
            } else if (Piece.cbefore(p.pce, q.pce)) {
                if (!invert) R.prepend(p.pce);
                p = p.prev;
            } else {
                if (invert) R.prepend(p.pce);
                p = p.prev;
            }
        }
        return R;        
    }
    
    public static PieceSet OpAfter(PieceSet x, PieceSet y, boolean invert) throws TypeCheckException {
        CheckCompatible(x, y);
        
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.next;
        Cell q = y.head.next;
        
        // search for the q with smallest end
        if (q != y.head) {
            Cell r = q.next;
            while (r != y.head && !Piece.cafter(r.pce, q.pce)) {
                if (Piece.endsafter(q.pce, r.pce))
                    q = r;
                r = r.next;
            }
        }
        
        while (p != x.head) {
            if (q == y.head) {
                if (invert) R.append(p.pce);
                p = p.next;
            } else if (Piece.cafter(p.pce, q.pce)) {
                if (!invert) R.append(p.pce);
                p = p.next;
            } else {
                if (invert) R.append(p.pce);
                p = p.next;
            }
        }
        return R;        
    }
    
    private static boolean DirectInsideCheck(Cell child, Cell parent, Cell phead) {
        Cell r = child;
        while(r.prev != phead && r.pce.beg.sno >= parent.pce.beg.sno)
            r = r.prev;
        while(r != phead && !Piece.cafter(r.pce, parent.pce)) {  // loop over everything inside parent
            if (r != child) {   // ignore child
                if (Piece.in(r.pce, parent.pce) && Piece.in(child.pce, r.pce))
                    return false;
            }
            r = r.next;
        }
        return true;
    }
    
    public static PieceSet OpDirectlyInside(PieceSet x, PieceSet y, boolean invert) throws TypeCheckException {
        /** Incorrect version that does not match with manual, detected and removed Sept 10, 1998
        PieceSet R = OpInside(x, y, invert);
        return OpInside(R, R, !invert);
        */
        
        CheckCompatible(x, y);
        
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.next;
        Cell q = y.head.next;
        
        Cell pend = x.head;
        Cell qend = y.head;
        
        while (p != pend) {
            if (q == qend) {
                if (invert) R.append(p.pce);
                p = p.next;
            } else if (Piece.cbefore(p.pce, q.pce)) {
                if (invert) R.append(p.pce);
                p = p.next;
            } else if (Piece.cbefore(q.pce, p.pce)) {
                q = q.next;
            } else {
                boolean found = false;
                Cell r = q;
                while (r != qend && !Piece.cafter(r.pce, p.pce)) {
                    if (Piece.in(p.pce, r.pce)) {
                        found = DirectInsideCheck(p, r, pend);
                        if (found) break;
                    }
                    r = r.next;
                }
                if (found ^ invert) R.append(p.pce);
                p = p.next;
            }
        }
        return R;        
    }

    private static boolean DirectContainCheck(Cell parent, Cell child, Cell phead) {
        Cell r = parent;
        while(r.prev != phead && r.pce.beg.sno == r.prev.pce.beg.sno)
            r = r.prev;
        while(r != phead && !Piece.cafter(r.pce, parent.pce)) {  // loop over everything inside parent
            if (r != parent) {   // ignore 
                if (Piece.contain(parent.pce, r.pce) && Piece.contain(r.pce, child.pce))
                    return false;
            }
            r = r.next;
        }
        return true;
    }
    
    public static PieceSet OpDirectlyContain(PieceSet x, PieceSet y, boolean invert) throws TypeCheckException {
        /** Incorrect version that does not match with manual, detected and removed Sept 10, 1998
        PieceSet R = OpContain(x, y, invert);
        return OpContain(R, R, !invert);
        */
        CheckCompatible(x, y);
        
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.next;
        Cell q = y.head.next;
        
        Cell pend = x.head;
        Cell qend = y.head;
        
        while (p != pend) {
            if (q == qend) {
                if (invert) R.append(p.pce);
                p = p.next;
            } else if (Piece.cbefore(p.pce, q.pce)) {
                if (invert) R.append(p.pce);
                p = p.next;
            } else if (Piece.cbefore(q.pce, p.pce)) {
                q = q.next;
            } else {
                boolean found = false;
                Cell r = q;
                while (r != qend && !Piece.cafter(r.pce, p.pce)) {
                    if (Piece.contain(p.pce, r.pce)) {
                        found = DirectContainCheck(p, r, pend);
                        if(found) break;
                    }
                    r = r.next;
                }
                if (found ^ invert) R.append(p.pce);
                p = p.next;
            }
        }
        return R;        
    }
    
    // sort x according to the end-points of pieces
    static private PieceSet OpReverse(PieceSet x) {
        PieceSet R = new PieceSet(x.page);
        
        Cell p = x.head.next;
        while (p != x.head) {
            R.insertRev(p.pce);
            p = p.next;
        }
        return R;
    }
    
    public static PieceSet OpDirectlyBefore(PieceSet x, PieceSet y, boolean invert) throws TypeCheckException {
        CheckCompatible(x, y);
        
        PieceSet R = new PieceSet(x.page);
        PieceSet xr = OpReverse(x);                 // reverse the x piece-set
        
        Cell p = xr.head.next;
        Cell q = y.head.next;
        
        while (p != xr.head) {
            if (q == y.head) {
                if (invert) R.insert(p.pce);
                p = p.next;
            } else if (!Piece.cafter(q.pce, p.pce)) {
                q = q.next;
            } else if (p.next != xr.head && Piece.cbefore(p.next.pce, q.pce)) {
                if (invert) R.insert(p.pce);
                p = p.next;
            } else {
                if (!invert) R.insert(p.pce);
                p = p.next;
            }
        }
        return R;
    }
    
    public static PieceSet OpDirectlyAfter(PieceSet x, PieceSet y, boolean invert) throws TypeCheckException { 
        CheckCompatible(x, y);
        
        PieceSet R = new PieceSet(x.page);
        PieceSet yr = OpReverse(y);                 // reverse the y piece-set
        
        Cell p = x.head.next;
        Cell q = yr.head.next;
        
        while (p != x.head) {
            if (q == yr.head) {
                if (invert) R.append(p.pce);
                p = p.next;
            } else if (!Piece.cafter(p.pce, q.pce)) {
                if (invert) R.append(p.pce);
                p = p.next;
            } else if (p.prev != x.head && Piece.cafter(p.prev.pce, q.pce)) {
                q = q.next;
            } else {
                if (!invert) R.append(p.pce);
                p = p.next;
            }
        }
        return R;
    }
        
    public static PieceSet OpWithout(PieceSet x, PieceSet y) throws TypeCheckException { 
        CheckCompatible(x, y);
        return x.page.Without(x, y);
    }    
    
    public static PieceSet OpRegionIntersect(PieceSet x, PieceSet y) throws TypeCheckException { 
        CheckCompatible(x, y);
        return x.page.RegionIntersect(x, y);        
    } 
    
    public static PieceSet OpFlatten(PieceSet x) { 
        return x.page.Flatten(x);        
    } 
    
    // all the pieces named name from x that are contained in p
    public static PieceSet OpSelect(PieceSet x, Piece p, String name) {
        PieceSet R = new PieceSet(x.page);
        
        Cell c = x.head.next;
        while (c != x.head) {
            if (Piece.contain(p, c.pce) && c.pce.name.equals(name)) R.append(c.pce);
            c = c.next;
        }
        return R;
    }

    // all the pieces with class c from x that are contained in p
    public static PieceSet OpSelectByClass(PieceSet x, Piece p, String c) {
        PieceSet R = new PieceSet(x.page);
        String[] classes = c.split(" ");

        Cell cell = x.head.next;
        boolean contains = true;

        Pattern pattern = null;
        Matcher matcher = null;

        while (cell != x.head) {
            if (Piece.contain(p, cell.pce)) {
                String attr = cell.pce.getAttr("class");
                if (attr != null) {
                    int i = 0;
                    for (i = 0; i < classes.length; i++) {
                        pattern = Pattern.compile("\\b" + classes[i] + "\\b");
                        matcher = pattern.matcher(attr);

                        if (matcher.find()) {
                            contains &= true;
                        } else {
                            contains &= false;
                        }
                    }
                } else
                    contains = false;
                
                
                if (contains)
                    R.append(cell.pce);
            }

            contains = true;
            cell = cell.next;
        }
        return R;
    }

    // all the pieces from x that are contained in p
    public static PieceSet OpSelect(PieceSet x, Piece p) {
        PieceSet R = new PieceSet(x.page);
        
        Cell c = x.head.next;
        while (c != x.head) {
            if (Piece.contain(p, c.pce)) R.append(c.pce);
            c = c.next;
        }
        return R;
    }
    
    //
    // misc
    //
    
    static void CheckCompatible(PieceSet x, PieceSet y) throws TypeCheckException {
        if (x.page != y.page)
            throw new TypeCheckException("piece-sets do not belong to the same page");
    }
    
    static void CheckCompatible(Page p, Piece x) throws TypeCheckException {
        if (p != x.page)
            throw new TypeCheckException("the piece does not belong to the page");
    }
}


class PieceSetEnumeration implements Enumeration
{
    Cell head, x;
    
    public PieceSetEnumeration(PieceSet S) {
        head = S.head;
        x = head.next;
    }
    
    public boolean hasMoreElements() {
       return x != head;
    }
    
    public Object nextElement() throws  NoSuchElementException {
        if (x == head)
            throw new NoSuchElementException();
        else {
            Piece p = x.pce;
            x = x.next;
            return p;
        }
    }

}
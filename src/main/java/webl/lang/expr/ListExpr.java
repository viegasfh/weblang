package webl.lang.expr;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

/*
    See "Amortization, Lazy evaluation, and Persistence: Lists with Catenation via
    Lazy Linking", Chris Okasaki, 36th Annual Symposium on Foundations of computer
    science, 1995.
    
    For class Queue, see "Real-time queue operations in pure Lisp", Hood & Melville,
    Information Processing Letters, 13(2):50-53, Nov 1981.

    The algorithmic costs are as follows (for lists X and Y with lengths x and y):

    concatenation of two arbitrary lists with +   O(1)
    First(X: list): any                           O(1)
    Rest(X: list): list                           O(1)
    PrintLn/Print of X                            O(x)
    X[index]                                      O(x)
    Select(X, from, to)                        O(x)

    The O(x) costs are only paid once for a specific list X, afterwards the cost is
    O(1) or O(X + to - from). This cost is needed to "flatten out" the list into an array
    from the internal pre-order tree used to store the list.
*/

public class ListExpr extends ValueExpr implements ContentEnumeration, Cloneable {
    private Expr        elem;
    private Queue       children;
    private int         len;
    
    Expr[]              data;
    
    static private ListExpr empty = new ListExpr();
    
    public ListExpr() {
        super(-1);
        elem = null;
        children = Queue.New();
        len = 0;
    }
    
    public ListExpr(Expr elem) {
        super(-1);
        if (elem == null)
            throw new InternalError("ListExpr with a null element");
        this.elem = elem;
        children = Queue.New();
        len = 1;
    }
    
    private ListExpr(Expr elem, Queue children, int len) {
        super(-1);
        this.elem = elem;
        this.children = children;
        this.len = len;
    }
    
    synchronized public int getSize() {
        return len;
    }
        
    synchronized public boolean isEmpty() {
        return len == 0;
    }
            
    public Object clone() {
        return this;
    }
    
    public String getTypeName() {
        return "list";
    }
            
    synchronized public String toString() {
        Flatten();
        
        StringBuffer s = new StringBuffer();
        s.append('[');
        for(int i = 0; i < len; i++) {
            s.append(data[i].toString());
            if (i < len - 1)
                s.append(", ");
        }
        s.append("]");
        return s.toString();
    }
    
    synchronized public int hashCode() {
        Flatten();
        int code = 0;
        for(int i = 0; i < len; i++) {
            code += data[i].hashCode();
        }
        return code;
    }
    
    synchronized public Expr getElementAt(int i) {
        if (i < 0 || i >= len)
            throw new IndexOutOfBoundsException();
        Flatten();
        return data[i];
    }
    
    synchronized public ListExpr getSubList(int from, int to) throws IndexOutOfBoundsException {
        Flatten();
        try {
            ListExpr L = new ListExpr();
            for (int i = from; i < to; i ++)
                L = L.Append(data[i]);
            return L;
        } catch(ArrayStoreException e) {
                throw new IndexOutOfBoundsException();
        }
    }
    
    synchronized public boolean contains(Expr x) {
        Flatten();
        for(int i=0; i < len; i++) {
            if (x.equals(data[i]))
                return true;
        }
        return false;
    }
    
    synchronized public boolean equals(Object obj) {
        if (obj instanceof ListExpr) {
            ListExpr L = (ListExpr)obj;
            if (L.getSize() == getSize()) {
                Flatten(); L.Flatten();
                for(int i = 0; i < len; i++)
                    if (!data[i].equals(L.data[i]))
                        return false;
                return true;
            }
        } 
        return false;
    }
    
    synchronized public ListExpr appendList(ListExpr L) {
        if (this.len == 0)
            return L;
        else if (L.len == 0)
            return this;
        else
            return Link(this, L);
    }
    
    synchronized public ListExpr Append(Expr elem) {
        return this.appendList(new ListExpr(elem));
    }
    
    synchronized public ListExpr Rest() {
        if (len == 0) 
            return empty;
        
        Force();
        if (children.Length() == 0)
            return empty;
        else if (children.Length() == 1)
            return children.First();
        else
            return new ListExpr(null, children, len - 1);
    }
    
    synchronized public Expr First() {
        if (len == 0) 
            return null;
        Force();
        return elem;
    }
    
    static private ListExpr Link(ListExpr T1, ListExpr T2) {
        T1.Force();
        return new ListExpr(T1.elem, T1.children.PushBack(T2), T1.len + T2.len);
    }
    
    static private void LinkAndMemoize(ListExpr R, ListExpr T1, ListExpr T2) {
        synchronized(R) {
            T1.Force();
            R.elem = T1.elem;
            R.children = T1.children.PushBack(T2);
            R.len = T1.len + T2.len;
        }
    }
    
    synchronized private void Force() {
        if (elem != null) return;
        
        ListExpr f = children.First();
        Queue r = children.Pop();
        
        ListExpr T2;
        if (children.Length() > 2)
            T2 = new ListExpr(null, r, len - f.len);
        else
            T2 = r.First();
        LinkAndMemoize(this, f, T2);
    }
    
    public Enumeration getContent() {
        Flatten();
        return new ListEnumerator(this);
    }

    private synchronized void Flatten() {
        if (data == null && len > 0) {
            data = new Expr[len];
            
            ListExpr L = this;
            for (int i = 0; i < len; i++) {
                data[i] = L.First();
                L = L.Rest();
            }
        }
    }
    
    synchronized public ListExpr sort(Context c, Expr callsite, AbstractFunExpr cmp) throws WebLException {
        if (len <= 1)
            return this;
        
        // copy everthing into a local array
        Expr dat[] = new Expr[len];
        
        ListExpr L = this;
        for (int i = 0; i < len; i++) {
            dat[i] = L.First();
            L = L.Rest();
        }
        
        // sort it !
        ListSorter S = new ListSorter(c, callsite, cmp, dat);
        S.sort(0, len - 1);
        
        // rebuild the list
        ListExpr R = new ListExpr();
        for (int i = 0; i < len; i++) {
            R = R.Append(dat[i]);
        }
        R.data = dat;
        return R;
    }
}

final class ListSorter {
    private Context         c;
    private Expr            callsite;
    private AbstractFunExpr cmp;
    private Expr[]          dat;
    private Vector          sortArgs = new Vector(2);
    
    public ListSorter(Context c, Expr callsite, AbstractFunExpr cmp, Expr[] dat) {
        this.c = c;
        this.callsite = callsite;
        this.cmp = cmp;
        this.dat = dat;
        sortArgs.addElement(Program.nilval);
        sortArgs.addElement(Program.nilval);
    }
    
    private final long compare(Object a, Object b) throws WebLException {
        sortArgs.setElementAt(a, 0);
        sortArgs.setElementAt(b, 1);
        Expr res = cmp.Apply(c, sortArgs, callsite);
        if (res instanceof IntExpr)
            return ((IntExpr)res).val;
        else
            throw new WebLException(c, callsite, "FunctionReturnTypeNotInteger", "function did not return an integer");
    }
    
    public final void sort(int p, int r) throws WebLException {
        if (p < r) {
            int q = partition(p,r);
            if (q == r) 
                q--;
            sort(p,q);
            sort(q+1,r);
        }
    }

    private final int partition (int lo, int hi) throws WebLException {
        Object pivot = dat[lo];
        while (true) {
            while (compare(dat[hi], pivot) >= 0 && lo < hi) hi--;
            while (compare(dat[lo], pivot) < 0 && lo < hi) lo++;
            if (lo < hi) {
                Expr T = dat[lo];
                dat[lo] = dat[hi];
                dat[hi] = T;
            } else
                return hi;
        }
    }
}

final class ListEnumerator implements Enumeration {
    ListExpr list;
    int count;

    ListEnumerator(ListExpr L) {
	    list = L;
	    count = 0;
    }

    public boolean hasMoreElements() {
	    return list.data != null && count < list.getSize();
    }

    public Object nextElement() {
	    if (count < list.getSize()) {
		    return list.data[count++];
	    }
	    throw new NoSuchElementException("ListEnumerator");
    }
}

class Queue {
    private SimpleList L = null;    // back of the queue
    private SimpleList R = null;    // front of the queue
    
    static private Queue empty = new Queue(null, null);
    
    private Queue(SimpleList L, SimpleList R) {
        this.L = L;
        this.R = R;
    }
    
    static public Queue New() {
        return empty;
    }
    
    public Queue PushBack(ListExpr elem) {
        return new Queue(SimpleList.Cons(elem, L), R);
    }
    
    public Queue PushFront(ListExpr elem) {
        return new Queue(L, SimpleList.Cons(elem, R));
    }
    
    public Queue Pop() {
        if (R == null)
            return new Queue(null, SimpleList.Rest(SimpleList.Reverse(L)));
        else
            return new Queue(L, SimpleList.Rest(R));
    }
    
    public ListExpr First() {
        if (R == null)
            return SimpleList.First(SimpleList.Reverse(L));
        else
            return SimpleList.First(R);
    }
    
    public int Length() {
        return SimpleList.Length(L) + SimpleList.Length(R);
    }
}

class SimpleList {
    private ListExpr elem;
    private SimpleList next;
    private int len;
    
    private SimpleList(ListExpr elem, SimpleList next) {
        this.elem = elem;
        this.next = next;
        if (next != null)
            len = next.len + 1;
        else
            len = 1;
    }
    
    static public int Length(SimpleList L) {
        if (L == null)
            return 0;
        else
            return L.len;
    }
    
    static public SimpleList Cons(ListExpr elem, SimpleList L) {
        return new SimpleList(elem, L);
    }
    
    static public ListExpr First(SimpleList L) {
        return L.elem;
    }
    
    static public SimpleList Rest(SimpleList L) {
        return L.next;
    }
    
    static public SimpleList Reverse(SimpleList L) {
        SimpleList R = null;
        while (L != null) {
            R = Cons(L.elem, R);
            L = L.next;
        }
        return R;
    }
}    

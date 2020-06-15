package webl.lang.expr;

import webl.lang.*;
import java.util.*;

/* Old implemetation:

public class SetExpr extends ValueExpr implements Cloneable, ContentEnumeration
{
    protected SetEntry table[];
    private int count;
    private int threshold;
    private static int initialCapacity = 64;
    private static float loadFactor = 0.75f;
    
    public SetExpr() {
        super(-1);
        table = new SetEntry[initialCapacity];
        count = 0;
        threshold = (int)(initialCapacity * loadFactor);
    }
    
    public String getTypeName() {
        return "set";
    }
    
    public synchronized void put(Expr o) {
        if(count == threshold) {
            rehash();
            put(o);
        } else {
            int hash = o.hashCode();
            int index = (hash & 0x7FFFFFFF) % table.length;
            SetEntry e = table[index];
            while (e != null) {
                if(e.hash == hash && e.obj.equals(o))
                    return;
                e = e.next;
            }
            e = new SetEntry(o, hash);
            e.next = table[index];
            table[index] = e;
            count++;
        }
    }
    
    public synchronized boolean contains(Expr o) {
        int hash = o.hashCode();
        int index = (hash & 0x7FFFFFFF) % table.length;
        SetEntry e = table[index];
        while (e != null) {
            if(e.hash == hash && e.obj.equals(o))
                return true;
            e = e.next;
        }
        return false;
    }
    
    // Remove an object from the set. Does nothing if object is not a member
    // of the set. 
    public synchronized void remove(Expr o) {
        int hash = o.hashCode();
        int index = (hash & 0x7FFFFFFF) % table.length;
        SetEntry e = table[index];
        SetEntry p = null;
        while (e != null) {
            if (e.hash == hash && e.obj.equals(o)) {
                if (p == null) 
                    table[index] = e.next;
                else 
                    p.next = e.next;
                count--;
                return;
            }
            p = e; e = e.next;
        }
    }
    
    //* Return size of set. 
    public synchronized int getSize() {
        return count;
    }
    
    private synchronized void rehash() {
        SetEntry n[] = new SetEntry[table.length * 2 + 1];
        threshold = (int)(n.length * loadFactor);
        
        for(int i = 0; i < table.length; i++) {
            SetEntry e = table[i];
            while (e != null) {
                SetEntry nxt = e.next;
                
                int index = (e.hash & 0x7FFFFFFF) % n.length;
                e.next = n[index];
                n[index] = e;
                
                e = nxt;
            }
        }
        table = n;  // switch over
    }
    
    public synchronized Enumeration elements() {
        return new SetEnumerator(this);
    }
    
    public synchronized Enumeration getContent() {
        return elements();
    }
    
    public synchronized String toString() {
        String eol = System.getProperty("line.separator");
        StringBuffer s = new StringBuffer();
        s.append("{");
        
        int count = 0;
        Enumeration enum = elements();
        while (enum.hasMoreElements()) {
            Object e = enum.nextElement();
            s.append(((Expr)e).toString());
            if(enum.hasMoreElements())
                s.append(", ");
            if (++count == 8) {
                count = 0;
                s.append(eol);
            }
        }
        s.append("}");
        return s.toString();
    }
    
    public synchronized void union(SetExpr x) {
        Enumeration enum = x.elements();
        while (enum.hasMoreElements()) {
            Object e = enum.nextElement();
            put((Expr)e);
        }
    }
    
    public synchronized void subtract(SetExpr x) {
        Enumeration enum = x.elements();
        while (enum.hasMoreElements()) {
            Object e = enum.nextElement();
            remove((Expr)e);
        }
    }
    
    public synchronized boolean eq(SetExpr x) {
        if (count != x.getSize())
            return false;
                        
        int found = 0;
        Enumeration enum = elements();
        while (enum.hasMoreElements()) {
            Object e = enum.nextElement();
            if(x.contains((Expr)e))
                found++;
        }
        return found == count;
    }
    
    public int hashCode() {
        int code = 0;
        Enumeration enum = elements();
        while (enum.hasMoreElements()) {
            Object e = enum.nextElement();
            code += e.hashCode();
        }
        return code;
    }
    
    public boolean equals(Object obj) {
        return (obj instanceof SetExpr) && (eq((SetExpr)obj));
    }
    
    public synchronized Object clone() {
        SetExpr n = new SetExpr();
        
        Enumeration enum = elements();
        while (enum.hasMoreElements()) {
            Object e = enum.nextElement();
            n.put((Expr)e);
        }
        return n;
    }
    
    public synchronized void intersect(SetExpr x) {
        Enumeration enum = elements();
        while (enum.hasMoreElements()) {
            Object e = enum.nextElement();
            if(!x.contains((Expr)e))
                remove((Expr)e);
        }
    }
    
}

class SetEntry
{
    int hash;
    Expr obj;
    SetEntry next;
    
    public SetEntry(Expr o, int hash) {
        obj = o;
        this.hash = hash;
    }
}

class SetEnumerator implements Enumeration
{
    SetExpr set;
    SetEntry entry;
    int i;
    
    public SetEnumerator(SetExpr s) {
        set = s;
        i = 0;
    }
    
    public boolean hasMoreElements() {
        if (entry != null)
            return true;
        while (i < set.table.length) {
            entry = set.table[i++];
            if (entry != null)
                return true;
        }
        return false;
    }
    
    public Object nextElement() {
        while (entry == null && i < set.table.length) {
            entry = set.table[i++];
        }
        if (entry != null) {
            Object o = entry.obj;
            entry = entry.next;
            return o;
        } else
            throw new NoSuchElementException("SetEnumerator");
    }   
}

*/
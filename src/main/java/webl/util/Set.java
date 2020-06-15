package webl.util;

import java.util.*;

public class Set implements Cloneable
{
    protected SetEntry table[];
    private int count;
    private int threshold;
    private static int initialCapacity = 64;
    private static float loadFactor = 0.75f;

    public Set() {
        table = new SetEntry[initialCapacity];
        count = 0;
        threshold = (int)(initialCapacity * loadFactor);
    }

    /** Insert object into set. */
    public synchronized void put(Object o) {
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

    /** Membership test. */
    public synchronized boolean contains(Object o) {
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

    /** Remove an object from the set. Does nothing if object is not a member
    of the set. */
    public synchronized void remove(Object o) {
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

    /** Return size of set. */
    public int getSize() {
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

    public synchronized String toString() {
        String s = "{";

        Enumeration enum = elements();
        while (enum.hasMoreElements()) {
            Object e = enum.nextElement();
            s += e.toString();
            if(enum.hasMoreElements())
                s += ", ";
        }
        return s + "}";
    }

    public synchronized void union(Set x) {
        Enumeration enum = x.elements();
        while (enum.hasMoreElements()) {
            Object e = enum.nextElement();
            put(e);
        }
    }

    public synchronized void subtract(Set x) {
        Enumeration enum = x.elements();
        while (enum.hasMoreElements()) {
            Object e = enum.nextElement();
            remove(e);
        }
    }

    public synchronized boolean eq(Set x) {
        if (count != x.getSize())
            return false;

        int found = 0;
        Enumeration enum = elements();
        while (enum.hasMoreElements()) {
            Object e = enum.nextElement();
            if(x.contains(e))
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
        return (obj instanceof Set) && (eq((Set)obj));
    }

    public synchronized Object clone() {
        Set n = new Set();

        Enumeration enum = elements();
        while (enum.hasMoreElements()) {
            Object e = enum.nextElement();
            n.put(e);
        }
        return n;
    }

    public synchronized void intersect(Set x) {
        Enumeration enum = elements();
        while (enum.hasMoreElements()) {
            Object e = enum.nextElement();
            if(!x.contains(e))
                remove(e);
        }
    }
}

class SetEntry
{
    int hash;
    Object obj;
    SetEntry next;

    public SetEntry(Object o, int hash) {
        obj = o;
        this.hash = hash;
    }
}

class SetEnumerator implements Enumeration
{
    Set set;
    SetEntry entry;
    int i;

    public SetEnumerator(Set s) {
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

package webl.lang.expr;

import webl.lang.*;
import webl.util.*;
import java.util.*;

public class SetExpr extends ValueExpr implements Cloneable, ContentEnumeration {
    private MultiSet mset;
    private Version  ver;
    private int      count;

    public SetExpr() {
        super(-1);
        mset = new MultiSet();
        ver = mset.GetHeadVersion();
    }

    private SetExpr(MultiSet mset, Version ver, int count) {
        super(-1);
        this.mset = mset;
        this.ver = ver;
        this.count = count;
    }

    public String getTypeName() {
        return "set";
    }

    public int getSize() {
        return count;
    }

    synchronized public SetExpr Put(Expr elem) {
        Version V = mset.MakeSubVersion(ver);
        int c = count + mset.Insert(elem, V);
        return new SetExpr(mset, V, c);
    }

    synchronized public SetExpr Remove(Expr elem) {
        Version V = mset.MakeSubVersion(ver);
        int c = count + mset.Remove(elem, V);
        return new SetExpr(mset, V, c);
    }

    synchronized public void DestructivePut(Expr elem) {
        count += mset.Insert(elem, ver);
    }

    synchronized public void DestructiveRemove(Expr elem) {
        count += mset.Remove(elem, ver);
    }

    synchronized public boolean Contain(Expr elem) {
        return mset.Member(elem, ver);
    }

    public synchronized Enumeration getContent() {
        return mset.getContent(ver);
    }

    static Counter unionc = new Counter("Set Union");

    synchronized public SetExpr Union(SetExpr S) {
        unionc.begin();
        Version V = mset.MakeSubVersion(ver);
        int c = count;

        Enumeration enumeration = S.getContent();
        while (enumeration.hasMoreElements()) {
            Expr elem = (Expr)enumeration.nextElement();
            c += mset.Insert(elem, V);
        }
        unionc.end();
        return new SetExpr(mset, V, c);
    }

    static Counter subtractc = new Counter("Set Subtract");

    synchronized public SetExpr Subtract(SetExpr x) {
        subtractc.begin();
        Version V = mset.MakeSubVersion(ver);
        int c = count;

        Enumeration enumeration = x.getContent();
        while (enumeration.hasMoreElements()) {
            Expr e = (Expr)enumeration.nextElement();
            c += mset.Remove(e, V);
        }
        subtractc.end();
        return new SetExpr(mset, V, c);
    }

    static Counter intersectc = new Counter("Set Intersect");

    synchronized public SetExpr Intersect(SetExpr x) {
        intersectc.begin();
        Version V = mset.MakeSubVersion(ver);
        int c = count;

        Enumeration enumeration = getContent();
        while (enumeration.hasMoreElements()) {
            Expr e = (Expr)enumeration.nextElement();
            if(!x.Contain(e))
                c += mset.Remove(e, V);
        }
        intersectc.end();
        return new SetExpr(mset, V, c);
    }

    synchronized public boolean eq(SetExpr x) {
        if (count != x.getSize())
            return false;

        int found = 0;
        Enumeration enumeration = getContent();
        while (enumeration.hasMoreElements()) {
            Expr e = (Expr)enumeration.nextElement();
            if(x.Contain(e))
                found++;
        }
        return found == count;
    }

    public synchronized String toString() {
        String eol = System.getProperty("line.separator");
        StringBuffer s = new StringBuffer();
        s.append("{");

        int count = 0;
        Enumeration enumeration = getContent();
        while (enumeration.hasMoreElements()) {
            Object e = enumeration.nextElement();
            s.append(((Expr)e).toString());
            if(enumeration.hasMoreElements())
                s.append(", ");
            if (++count == 8) {
                count = 0;
                s.append(eol);
            }
        }
        s.append("}");
        return s.toString();
    }

    public boolean equals(Object obj) {
        return (obj instanceof SetExpr) && (eq((SetExpr)obj));
    }

    synchronized public Object clone() {
        SetExpr n = new SetExpr();

        Enumeration enumeration = getContent();
        while (enumeration.hasMoreElements()) {
            Expr e = (Expr)enumeration.nextElement();
            n.mset.Insert(e, n.ver);
        }
        return n;
    }

    synchronized public int hashCode() {
        int code = 0;
        Enumeration enumeration = getContent();
        while (enumeration.hasMoreElements()) {
            Object e = enumeration.nextElement();
            code += e.hashCode();
        }
        return code;
    }
}

final class MultiSet {
    protected Entry[]       table;
    private int             count;
    private int             threshold;
    private static int      initialCapacity = 8;
    private static float    loadFactor = 0.75f;

    VersionTree     vt;

    public MultiSet() {
        vt = new VersionTree();

        table = new Entry[initialCapacity];
        count = 0;
        threshold = (int)(initialCapacity * loadFactor);
    }

    public Version GetHeadVersion() {
        return vt.GetHeadVersion();
    }

    public Version MakeSubVersion(Version V) {
        return vt.MakeSubVersion(V);
    }

    private Entry GetEntry(Entry e, Expr elem, int hash) {
        while (e != null) {
            if (e.hash == hash && e.elem.equals(elem))
                return e;
            e = e.next;
        }
        return null;
    }

    synchronized public int Insert(Expr elem, Version V) {
        if (count == threshold) Rehash();

        int hash = elem.hashCode();
        int index = (hash & 0x7FFFFFFF) % table.length;
        Entry e = GetEntry(table[index], elem, hash);

        if (e == null) {
            e = new Entry(elem, hash);                  // create a new entry
            e.next = table[index];
            table[index] = e;
            count++;

            e.AddOp(true, V);
            return 1;
        }
        Operation op = e.GetOperation(vt, V);
        if (op == null) {
            e.AddOp(true, V);
            return 1;
        } else if (op.V == V) {
            if (!op.ins) {
                op.ins = true;                              // modify in place
                return 1;
            } else
                return 0;
        } else if (!op.ins) {                             // was a delete before.
            e.AddOp(true, V);
            return 1;
        } else
            return 0;
    }

    synchronized public int Remove(Expr elem, Version V) {
        if (count == threshold) Rehash();

        int hash = elem.hashCode();
        int index = (hash & 0x7FFFFFFF) % table.length;
        Entry e = GetEntry(table[index], elem, hash);

        if (e == null)
            return 0;
        Operation op = e.GetOperation(vt, V);
        if (op == null)
            return 0;
        else if (op.V == V) {
            if (op.ins) {
                op.ins = false;
                return -1;
             } else
                return 0;
        } else if (op.ins) {
            e.AddOp(false, V);
            return -1;
        } else
            return 0;
    }

    public boolean Member(Expr elem, Version V) {
        int hash = elem.hashCode();
        int index = (hash & 0x7FFFFFFF) % table.length;
        Entry e = GetEntry(table[index], elem, hash);
        if (e == null) return false;
        Operation op = e.GetOperation(vt, V);
        return op != null && op.ins;
    }

    static Counter rehashc = new Counter("Set Rehash");

    private synchronized void Rehash() {
        rehashc.begin();

        Entry n[] = new Entry[table.length * 2 + 1];
        threshold = (int)(n.length * loadFactor);

        for(int i = 0; i < table.length; i++) {
            Entry e = table[i];
            while (e != null) {
                Entry nxt = e.next;

                int index = (e.hash & 0x7FFFFFFF) % n.length;
                e.next = n[index];
                n[index] = e;

                e = nxt;
            }
        }
        table = n;  // switch over
        rehashc.end();
    }

    public synchronized Enumeration getContent(Version V) {
        return new SetEnumerator(this, V);
    }
}

final class Entry {
    Expr        elem;
    int         hash;
    Operation   operations;

    Entry       next;

    public Entry(Expr elem,int hash) {
        this.elem = elem;
        this.hash = hash;
    }

    public void AddOp(boolean ins, Version V) {
        Operation op = new Operation(ins, V);      // associate a new version with it
        op.next = operations;
        operations = op;
    }

    public Operation GetOperation(VersionTree vt, Version V) {
        Operation R = null;

        Operation o = operations;
        while (o != null) {
            if (vt.SubVersionOf(V, o.V)) {
                if (R == null)
                    R = o;
                else if (vt.SubVersionOf(o.V, R.V))
                    R = o;
            }
            o = o.next;
        }
        return R;
    }
}

final class Operation {
    boolean         ins;
    Version         V;
    Operation       next;

    public Operation(boolean ins, Version V) {
        this.ins = ins;
        this.V = V;
    }
}

class SetEnumerator implements Enumeration
{
    MultiSet   mset;
    Version    V;

    Entry       entry;
    Expr        R = null;
    int         i = 0;

    public SetEnumerator(MultiSet mset, Version V) {
        this.mset = mset;
        this.V = V;
        Adv();
    }

    private void Adv() {
        R = null;
        while (R == null) {
            if (entry == null) {
                if (i < mset.table.length)
                    entry = mset.table[i++];
                else
                    break;
            } else {            // entry not null
                Operation op = entry.GetOperation(mset.vt, V);
                if (op != null && op.ins)
                    R = entry.elem;
                entry = entry.next;
            }
        }
    }

    public boolean hasMoreElements() {
        return R != null;
    }

    public Object nextElement() {
        if (R == null)
            throw new NoSuchElementException("SetEnumerator");
        Object r = R;
        Adv();
        return r;
    }
}

final class VersionTree
{
    protected long          inserts = 0;
    protected long          strategy = 1;
    protected Version       top;
    protected Version       dummy;
    protected long          versioncount = 0;

    public VersionTree() {
        top = new Version();
        dummy = new Version();
        top.e = dummy;
        top.n = dummy;
        dummy.p = top;
        top.no = 0;
        dummy.no = Long.MAX_VALUE;
}

    public Version GetHeadVersion() {
        return top;
    }

    public boolean SubVersionOf(Version X, Version Y) {
        return Y.no <= X.no && X.e.no <= Y.e.no;
    }

    public Version MakeSubVersion(Version V) {
        Version L = V;
        Version R = V.n;
        boolean first = V.n == V.e;

        Version N = new Version();
        N.n = R; N.p = L; N.e = R;
        L.n = N; R.p = N;
        versioncount++;

        long inc = (R.no - L.no) / (1L << 16);
        long num;                   // stores the "hint" no of the new node

        if (strategy == 1) {
            if (first)
                num = L.no + inc;
            else
                num = R.no - inc;
        } else {
            if (first)
                num = R.no - inc;
            else
                num = L.no + inc;
        }
        if (num > L.no && num < R.no)  {
            N.no = num;
            inserts++;
        } else
            Renumerationber();
        return N;
    }

    private void Renumerationber() {           // simple implementation that renumerationbers everything
        strategy = strategy * -1;       // switch strategy

        Version n = top;
        long no = 0;
        long inc = Long.MAX_VALUE / (versioncount + 3);
        while (n != null) {
            n.no = no;
            no += inc;
            n = n.n;
        }
        inserts = 1;
    }

    private void Verify() {
        long c = 0;
        Version n = top.n;
        while (n != dummy) {
            if (n.no <= n.p.no)
                throw new InternalError();
            c++;
            n = n.n;
        }
        if (c != versioncount)
            throw new InternalError();
    }

    public void SelfTest() {
        Log.println("Version Tree self test beginning");
        long c = 1000000;

        Version T = GetHeadVersion();
        Log.println("Sub Pattern");
        Version V = T;
        for(long i = 0; i < c; i++) {
            Version W = MakeSubVersion(V);
            if (i % 10000 == 0) Verify();

            if (!SubVersionOf(W, V))
                throw new InternalError();
            if (SubVersionOf(V, W))
                throw new InternalError();
            V = W;
        }
        Verify();
        Log.println("Across Pattern");
        for(long i = 0; i < c; i++) {
            Version W = MakeSubVersion(V);
            if (i % 10000 == 0) Verify();
            if (!SubVersionOf(W, V))
                throw new InternalError();
            if (SubVersionOf(V, W))
                throw new InternalError();
        }

        Verify();
        Log.println("Across Pattern 2");
        V = T;
        for(long i = 0; i < c; i++) {
            Version W = MakeSubVersion(V);
            if (i % 10000 == 0) Verify();
            if (!SubVersionOf(W, V))
                throw new InternalError();
            if (SubVersionOf(V, W))
                throw new InternalError();
        }

        Verify();
        V = T;
        Log.println("Random Pattern");
        for(long i = 0; i < c; i++) {
            if (Math.random() < 0.5) {
                Version W = MakeSubVersion(V);
                if (!SubVersionOf(W, V))
                    throw new InternalError();
                if (SubVersionOf(V, W))
                    throw new InternalError();
                V = T;
            } else {
                Version W = MakeSubVersion(V);
                if (!SubVersionOf(W, V))
                    throw new InternalError();
                if (SubVersionOf(V, W))
                    throw new InternalError();
            }
            if (i % 10000 == 0) Verify();
        }
        Verify();
        Log.println("Version Tree self test completed " + versioncount);
    }
}

final class Version
{
    long        no;
    Version     n, p, e;
}

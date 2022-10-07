package webl.lang.expr;

import webl.lang.*;
import webl.util.*;
import java.util.*;

public class ObjectExpr extends ValueExpr implements Cloneable, ContentEnumeration
{
    protected ObjectExpr         proto=null;      // read-only prototype of this object
                                                  //   (null if no prototype)
    protected OrderedHashtable   hash=null;       // note: hashtable is only allocated when really needed
    
    
    public ObjectExpr(int capacity) {
        super(-1);
        if (capacity > 0)
            hash = new OrderedHashtable(capacity);
    }

    public ObjectExpr() {
        this(0);
    }

    public String getTypeName() {
        return "object";
    }
    
    protected static void Copy(ObjectExpr source, ObjectExpr dest) {
        if (source.hash != null)
            dest.hash = (OrderedHashtable)(source.hash.clone());
        dest.proto = source.proto;
    }
    
    synchronized public Object clone() {
        ObjectExpr obj = new ObjectExpr();
        Copy(this, obj);
        return obj;
    }

/* Object printer without recursive check
    synchronized public String toString() {
        String eol = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer();
        buf.append("[. ");
        
        int count = 0;
        Enumeration e = EnumKeys();
        while(e.hasMoreElements()) {
            Expr n = (Expr)e.nextElement();
            buf.append(n).append(" = ").append(get(n));
            if (e.hasMoreElements())
                buf.append(", ");
            if (++count == 8) {
                count = 0;
                buf.append(eol);
            }
        }
        buf.append(" .]");
        return buf.toString();
    }
*/

    private StringBuffer buf = null;
    
    synchronized public String toString() {
        if (buf == null) {
            try {
                String eol = System.getProperty("line.separator");
                buf = new StringBuffer();
                buf.append("[. ");
                
                int count = 0;
                Enumeration e = EnumKeys();
                while(e.hasMoreElements()) {
                    Expr n = (Expr)e.nextElement();
                    buf.append(n).append(" = ").append(get(n));
                    if (e.hasMoreElements())
                        buf.append(", ");
                    if (++count == 8) {
                        count = 0;
                        buf.append(eol);
                    }
                }
                buf.append(" .]");
                return buf.toString();
            } finally {
                buf = null;
            }
        } else {
            if (buf.charAt(0) != '/') {
                buf.insert(0, "/*" + hashCode() + "*/");
            }
            return "<REF " + hashCode() + ">";
        }
    }
    
    synchronized public boolean empty() {
        return (hash == null || hash.size() == 0) && (proto == null || proto.empty());
    }

    synchronized public Enumeration EnumKeys() {
        return new ObjectEnumerator(this);
    }
    
    synchronized public Expr get(Expr key) {
        if (hash != null) {
            Object n = hash.get(key);
            if (n != null) return (Expr)n;
        }
        if (proto != null)
            return proto.get(key);
        return null;
    }

    synchronized public Expr get(String key) {
        return get(Program.Str(key));
    }
    
    synchronized public boolean def(Expr key, Expr val) {
        if (hash == null)
            hash = new OrderedHashtable(4);
        hash.put(key, val);
        return true;
    }
    
    synchronized public boolean def(String name, Expr val) {
        return def(Program.Str(name), val);
    }
    
    synchronized public boolean set(Expr key, Expr val) {
        Expr t = get(key);                      // first check if already defined
        if (t != null) {
            if (hash == null)
                hash = new OrderedHashtable(4);
            hash.put(key, val);
            return true;
        } else
            return false;
    }
    
    synchronized public boolean remove(Expr key) {
        if (hash != null) 
            return hash.remove(key);
        else
            return false;
    }
    
    synchronized public Enumeration getContent() {
        return EnumKeys();
    }
}


class ObjectEnumerator implements Enumeration
{
    ObjectExpr obj;
    Enumeration meE = null, protoE = null;
    
    Object lookahead;
    
    public ObjectEnumerator(ObjectExpr obj) {
        this.obj = obj;
        if (obj.hash != null) 
            meE = obj.hash.keys();
        if (obj.proto != null)
            protoE = obj.proto.EnumKeys();
        advance();    
    }
    
    public boolean hasMoreElements() {
        return lookahead != null;
    }
    
    public Object nextElement() throws NoSuchElementException {
        if (lookahead == null)
            throw new NoSuchElementException();
        Object R = lookahead;
        advance();          // overwrites lookahead
        return R;
    }
    
    private void advance() {
        lookahead = null;
        while (meE != null || protoE != null) {
            
            // first do all my own instance variables
            if (meE != null) {
                if (meE.hasMoreElements())
                    try {
                        lookahead = meE.nextElement();
                        return;
                    } catch (NoSuchElementException e) {
                        meE = null;
                    }
                else
                    meE = null;
            }
            // then do all of the prototype
            if (protoE != null) {
                if (protoE.hasMoreElements()) {
                    lookahead = protoE.nextElement();
                    if (obj.hash != null && obj.hash.containsKey(lookahead))   // filter duplicate elements
                        lookahead = null;
                } else
                    protoE = null;
            }
            
            if (lookahead != null) return;
        }
    }

}
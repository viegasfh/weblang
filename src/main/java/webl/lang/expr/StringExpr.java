package webl.lang.expr;

import webl.lang.*;
import webl.util.*;
import java.util.*;

abstract public class StringExpr extends ValueExpr implements ContentEnumeration
{
    static public StringExpr NewString(String s) {
        return new ConcreteString(s);
    }
    
    static public StringExpr Concat(StringExpr a, StringExpr b) {
        return new ConsString(a, b);
    }
    
    static public StringExpr Concat(StringExpr a, CharExpr b) {
        return new ConsString(a, b);
    }
    
    static public StringExpr Concat(CharExpr a, StringExpr b) {
        return new ConsString(a, b);
    }
    
    static public StringExpr Concat(CharExpr a, CharExpr b) {
        return new ConsString(a, b);
    }
    
    static public StringExpr Concat(StringExpr a, String b) {
        return new ConsString(a, b);
    }
    
    
    protected StringExpr() {
        super(-1);
    }

    public String getTypeName() {
        return "string";
    }
    
    abstract public String val();
    
    public String toString() {
        return "\"" + val() + "\"";
    }
    
    public String print() {
        return val();
    }
    
    public int hashCode() {
        return val().hashCode();
    }
    
    public boolean equals(Object obj) {
        return (obj instanceof StringExpr) && ((StringExpr)obj).val().compareTo(val()) == 0;
    }
    
    public Enumeration getContent() {
        return new StringEnumerator(val());
    }    
}

class ConcreteString extends StringExpr
{
    String str;
    
    public ConcreteString(String s) {
        super();
        str = s;
    }
    
    public String val() {
        return str;
    }
}    
    
class ConsString extends StringExpr
{
    Object a, b;
    
    public ConsString(Object a, Object b) {
        this.a = a;
        this.b = b;
    }
    
    synchronized public String val() {
        if (b == null)
            return (String)a;
        else {                      // flatten out the tree
            StringBuffer s = new StringBuffer();
            Stack stck = new Stack();
            
            stck.push(b);
            stck.push(a);
            int count = 0;
            
            while(!stck.empty()) {
                Object o = stck.pop();
                if (o instanceof ConsString) {
                    if (((ConsString)o).b != null)
                        stck.push(((ConsString)o).b);
                    stck.push(((ConsString)o).a);
                } else if (o instanceof CharExpr) {
                    s.append(((CharExpr)o).ch);
                    count++;
                } else if (o instanceof ConcreteString) {
                    s.append(((ConcreteString)o).val());
                    count++;
                } else if (o instanceof String) {
                    s.append((String)o);
                    count++;
                } else
                    throw new InternalError("string construction error" + o.getClass().getName());
            }
            
            a = s.toString();
            b = null;     // to help garbage collector
            
            return (String)a;
        }
    }
}


class StringEnumerator implements Enumeration
{
    private String s;
    private int len, pos=0;
    
    public StringEnumerator(String s) {
        this.s = s;
        len = s.length();
    }
    
    public boolean hasMoreElements() {
        return pos != len;
    }
    
    public Object nextElement() throws NoSuchElementException {
        if (pos == len)
            throw new NoSuchElementException();
        return Program.Chr(s.charAt(pos++));
    }
}
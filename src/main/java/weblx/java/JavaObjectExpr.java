package weblx.java;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class JavaObjectExpr extends ObjectExpr implements Cloneable, ContentEnumeration
{
    public Object       obj;            // the Java object being wrapped
    public ClassDesc    desc;           // description of the wrapped object
    
    public JavaObjectExpr(Object obj) {
        super(0);
        this.obj = obj;
        desc = ClassDesc.Get(obj.getClass());
    }

    public String getTypeName() {
        return "j-object";
    }
    
    // synchronized public String toString()   inherited
    
    synchronized public Object clone() {
        return null;
    }

    synchronized public boolean empty() {
        return desc.empty();
    }

    synchronized public Enumeration EnumKeys() {
        return desc.EnumKeys();
    }
    
    synchronized public Expr get(Expr key) {
       return desc.getField(obj, key);
    }

    synchronized public Expr get(String key) {
        return desc.getField(obj, Program.Str(key));
    }
    
    synchronized public boolean def(Expr key, Expr val) {
        return false;
    }
    
    synchronized public boolean def(String name, Expr val) {
        return false;
    }
    
    synchronized public boolean set(Expr key, Expr val) {
        return desc.setField(obj, key, val);
    }
    
    synchronized public Enumeration getContent() {
        return EnumKeys();
    }
    
}

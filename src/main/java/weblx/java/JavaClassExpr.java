package weblx.java;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class JavaClassExpr extends ObjectExpr implements Cloneable, ContentEnumeration
{
    public ClassDesc    desc;           // description of the wrapped object
    
    public JavaClassExpr(Class clss) {
        super(0);
        desc = ClassDesc.Get(clss);
    }

    // public String getTypeName()  inherited
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
       return desc.getField(null, key);
    }

    synchronized public Expr get(String key) {
        return desc.getField(null, Program.Str(key));
    }
    
    synchronized public boolean def(Expr key, Expr val) {
        return false;
    }
    
    synchronized public boolean def(String name, Expr val) {
        return false;
    }
    
    synchronized public boolean set(Expr key, Expr val) {
        return desc.setField(null, key, val);
    }
    
    synchronized public Enumeration getContent() {
        return EnumKeys();
    }
    
}

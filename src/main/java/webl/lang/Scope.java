package webl.lang;

import webl.lang.expr.*;
import webl.util.*;
import java.util.*;

public class Scope
{
    public Machine     machine;
    Scope       up;                  // next upper level Scope
    int         nestinglevel;        // how deep is this Scope nested in the stack of VarTables connected by up
    private int depth = 0;           // how many variables on this activation frame
    
    ScopeVar    top = null;         // variables currently visible in scope
    ScopeVar    all = null;         // all the variables in the scope
    
    public Scope(Machine machine) {
        this.machine = machine;
        nestinglevel = 0;
    }
    
    public Scope(Scope up) {
        this.machine = up.machine;
        this.up = up;
        nestinglevel = up.nestinglevel + 1;
    }
    
    public void OpenBlock() {
        top = new ScopeVar(top);
    }
    
    public void CloseBlock() {
        while (!top.SubScopeToken()) top = top.next;
        top = top.next;
    }
    
    public VarExpr lookup(String name) {
        Scope T = this;
        int lev = 0;
        
        ScopeVar v = T.localLookup(name);
        while (v == null) {
            T = T.up;
            if (T == null) return null;
            lev++;
            v = T.localLookup(name);
        }
        return new VarExpr(v.name, v.offset, lev);
    }
    
    public int importVariable(String name) {
        ScopeVar v = localLookup(name);
        if (v != null && v.export)
            return v.offset;
        else
            return -1;
    }
    
    public VarExpr define(String name, boolean export) {
        ScopeVar p = top;
        while (p != null) {
            if (p.SubScopeToken()) break;
            if (p.name.equals(name)) break;
            p = p.next;
        }
        if (p == null || p.SubScopeToken()) {           // no such variable
            top = new ScopeVar(top, name, depth, export);
            all = new ScopeVar(all, name, depth, export);
            return new VarExpr(name, depth++, 0);
        } else
            return null;
    }
      
    public VarExpr define(String name) {
        return define(name, false);
    }
    
    public int size() { 
        return depth;
    }
        
    public boolean TopLevel() {
        if (nestinglevel != 1) return false;
        
        ScopeVar p = top;
        while (p != null) {
            if (p.SubScopeToken()) return false;
            p = p.next;
        }
        return true;
    }
    
    public String toString() {
        String eol = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer();
        ScopeVar p = all;
        while(p != null) {
            buf.append("var ").append(p.name).append(" ").append(p.offset).append(eol);
            p = p.next;
        }        
        return buf.toString();
    }

    private String Trim(String s) {
        if (s.length() > 128)
            return s.substring(0, 128) + "...";
        else
            return s;
    }
    
    public String toString(Context c) {
        String eol = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer();
        ScopeVar p = all;
        while(p != null) {
            Expr val = c.binding[p.offset];
            buf.append("   ").append(p.offset).append("  ").append(p.name);
            if (val instanceof ValueExpr)
                buf.append(": ").append(((ValueExpr)val).getTypeName());
            buf.append("=").append(Trim(val.toString())).append(eol);
            p = p.next;
        }        
        return buf.toString();
    }
    
    protected ScopeVar localLookup(String name) {
        ScopeVar p = top;
        while (p != null) {
            if (p.name != null && p.name.equals(name)) return p;
            p = p.next;
        }
        return null;
    }    
    
}

class ScopeVar {
    ScopeVar next;
    String name;
    int offset;
    boolean export;
    
    public ScopeVar(ScopeVar next) {
        this.next = next;
        this.name = null;
    }
    
    public ScopeVar(ScopeVar next, String name, int offset, boolean export) {
        this.next = next;
        this.name = name;
        this.offset = offset;
        this.export = export;
    }
     
    public boolean SubScopeToken() {
        return name == null;
    }
}

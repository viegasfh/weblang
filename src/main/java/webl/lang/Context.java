package webl.lang;

import java.util.*;
import java.io.*;
import webl.lang.expr.*;

public class Context
{
    public Context next;             // to outer context
    public Context caller;           // calling context (if any)
    public Module module;            // module this context belongs to
    public Scope scope;              // used to identify variable names
    public Expr creationsite;        // place in the program where context was created
    public Expr binding[];           // variable bindings

    public Context(Context outer, Context caller, Module mod, Scope scope, Expr creationsite) {
        next = outer;
        this.caller = caller;
        module = mod;
        
        this.scope = scope;
        this.creationsite = creationsite;
        int stackdepth = scope.size();
        binding = new Expr[stackdepth];   // allocate the variable space
        
        // reset the stack contents
        for(int i = 0; i < stackdepth; i++)
            binding[i] = Program.nilval;
    }

    public Context(Context outer, Context caller, Scope scope, Expr creationsite) {
        this(outer, caller, outer.module, scope, creationsite);
    }
    
    public Expr lookup(String varname) {
        VarExpr V = scope.lookup(varname);
        if (V != null) {
            return V.lookup(this);
        } else
            return null;
    }
    
    public void assign(String name, Expr val) {
        VarExpr x = scope.lookup(name);
        if (x != null) 
            x.assign(this, val);
        else
            throw new Error("internal error");        
    }
    
    // Expands a string containing variable references of the
    // form $XYZ or ${XYZ}.
    public String expand(String str) throws IOException {
        StringBuffer s = new StringBuffer();
        
        Reader R = new StringReader(str);
        int ch = R.read();
        while (ch != -1) {
            if (ch == '$') {
                ch = R.read();
                if (ch == '$') {
                    s.append('$');
                    ch = R.read();;
                } else {
                    StringBuffer id = new StringBuffer();
                    boolean escaped = false;
                    if (ch == '{') {
                        ch = R.read();
                        escaped = true;
                    }
                    while (Ident(ch)) {
                        id.append((char)ch);
                        ch = R.read();
                    }
                    if (escaped && ch == '}') ch = R.read();
        
                    Expr v = lookup(id.toString());
                    if (v != null)
                        s.append(v.print());
                    else
                        s.append("(nil)");
                }
            } else {
                s.append((char)ch);
                ch = R.read();;
            }
        }      
        return s.toString();
    }
    
    private final boolean Ident(int ch) {
        return (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9');
    }    
    
}
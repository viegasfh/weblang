package webl.lang;

import webl.lang.*;
import webl.lang.expr.*;

public class Module
{
    public String       name;
    public Scope        scope;
    public Context      context;
    public long         lastmodified;           // last modified date of the module file (o if not known)
    
    public Module(Machine m, String name, Scope scope, long lastmodified) {
        this.name = name;
        this.scope = scope;
        this.lastmodified = lastmodified;
        context = new Context(m.universe, null, this, scope, Program.Str("module " + name));
    }

    public VarExpr importVariable(String varname) {
        int offset = scope.importVariable(varname);
        if (offset != -1)
            return new VarExpr(name + "_" + varname, this, offset);
        else
            return null;
    }
    
    public int hashCode() {
        return name.hashCode();
    }
    
    public boolean equals(Object obj) {
        return (obj instanceof Module) && ((Module)obj).name.equals(name);
    }    
}
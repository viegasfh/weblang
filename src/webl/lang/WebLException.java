package webl.lang;

import webl.lang.expr.*;
import java.util.*;


public class WebLException extends Exception
{
    public Expr callsite;       // expression that failed
    public Context context;     // context of failure
    protected ObjectExpr obj;      // exception object
    public String msg;
    
    public WebLException(Context c, Expr callsite, ObjectExpr exobj) {
        super("WebLException");
        this.context = c;
        this.callsite = callsite;
        this.obj = exobj;
    }
    
    public WebLException(Context c, Expr callsite, String type, String msg) {
        super("WebLException");
        this.context = c;
        this.callsite = callsite;
        this.msg = msg;
        obj = new ObjectExpr();
        obj.def("msg", Program.Str(msg));
        obj.def("type", Program.Str(type));
        
        if (context != null && context.module != null)
            obj.def("module", Program.Str(context.module.name));
            
        if (callsite != null && callsite.ppos != -1)
            obj.def("line", Program.Int(callsite.ppos));
    }
    
/**
@return ObjectExpr representing the exception.
*/
    public ObjectExpr MakeObject() {
        return obj;
    }

    public String StackTrace() {
        String eol = System.getProperty("line.separator");
        StringBuffer s = new StringBuffer();
        
        int count = 25;             // only print maximum 25 stack traces
        Context c = context;
        while (c != null && count-- > 0) {
            if (c.creationsite != null) {
                s.append("    ");
                s.append(c.creationsite.print());
                if (c.caller != null) {
                    s.append("   (");
                    if (c.caller.module != null) 
                        s.append(c.caller.module.name).append(", ");
                    
                    if (c.creationsite.ppos != 0)
                        s.append("line " + c.creationsite.ppos);
                    s.append(")");
                }
                s.append(eol);
            } else
                s.append("--an unknown location--").append(eol);
            c = c.caller;
        }
        if (c != null)
            s.append("    ...");
        return s.toString();
    }
    
/**
@return Report describing the exception type and location.
*/
    public String report() {
        String eol = System.getProperty("line.separator");
        String s = "WebL exception: " + obj.toString();
        if (context != null) {
            if (context.module != null)
                s += eol + "in module " + context.module.name; 
        }
        if (callsite != null) {
            if (callsite.ppos != -1)
                s += eol + "at line " + callsite.ppos;
            s += eol + "while evaluating " + callsite.toString();
        }       
        if (context != null && context.scope != null) {
            s += eol + eol + "Variables in context:"
                 + eol + context.scope.toString(context);
        }
        s += eol + "Stack trace:" + eol + StackTrace();
        return s;
    }
}
package webl.lang;

import webl.lang.expr.*;
import java.util.*;


public class WebLReturnException extends RuntimeException
{
    public Expr val, callsite;
    public Context context;
    
    public WebLReturnException(Context context, Expr callsite, Expr val) {
        super("WebLReturnException");
        this.val = val;
        this.context = context;
        this.callsite = callsite;
    }
    
    public String toString() {
        String eol = System.getProperty("line.separator");
        String s = "WebL return exception: ";
        if (context != null) {
            if (context.module != null)
                s += eol + "in module " + context.module.name; 
        }
        if (callsite != null) {
            if (callsite.ppos != -1)
                s += eol + "at line " + callsite.ppos;
            s += eol + "while evaluating " + callsite.toString();
        }       
        return s;        
    }
}    
    
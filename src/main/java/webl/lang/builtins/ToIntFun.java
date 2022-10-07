package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;
import java.lang.*;

public class ToIntFun extends AbstractFunExpr
{
    public String toString() {
        return "<ToInt>";
    }
    
    // take out , characters
    String cleanup(String s) {
        StringBuffer r = new StringBuffer();
        int len = s.length();
        for(int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            if (ch != ',')
                r.append(ch);
        }
        return r.toString();
    }

    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        Expr t = ((Expr)(args.elementAt(0))).eval(c);
        
        try {
            if (t instanceof StringExpr) {
                Long l = new Long(cleanup(((StringExpr)t).val()));
                return Program.Int(l.longValue());
            } else if (t instanceof CharExpr) {
                char ch = ((CharExpr)t).ch;
                return Program.Int((long)ch & 0xFFFF);
            } else if (t instanceof RealExpr) {
                Double d = new Double(((RealExpr)t).val);
                return Program.Int(d.longValue());  
            }  else if (t instanceof IntExpr) {
                return t;  
            }
        } catch(NumberFormatException n) {
            throw new WebLException(c, callsite, "ArgumentError", "conversion to integer failed");
        }
        throw new WebLException(c, callsite, "ArgumentError", "toint function expects a string, char, or real as argument");
    }
}

package weblx.url;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class EncodeFun extends AbstractFunExpr
{
    public String toString() {
        return "<Url_Encode>";
    }

    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        String s = StringArg(c, args, callsite, 0);
        return Program.Str(Encode(s));
    }
    
    // does not know about unicode !
    private String Encode(String str) {
        StringBuffer s = new StringBuffer();
        int len = str.length();
        for(int i = 0; i < len; i++) {
            char ch = str.charAt(i);
            if (ch == ' ')
                s.append('+');
            else if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z')
                s.append(ch);
            else if (ch >= '0' && ch <= '9')
                s.append(ch);
            else {
                s.append('%');
                s.append(Hex((ch & 0xF0) >> 4));
                s.append(Hex(ch & 0x0F));
            }
        }
        return s.toString();
    }
    
    private char Hex(int v) {
        if (v < 10)
            return (char)('0' + v);
        else
            return (char)(v - 10 + 'A');
    }
}
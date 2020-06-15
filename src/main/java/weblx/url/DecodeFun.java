package weblx.url;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class DecodeFun extends AbstractFunExpr
{
    public String toString() {
        return "<Url_Decode>";
    }

    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        String s = StringArg(c, args, callsite, 0);
        return Program.Str(Decode(s));
    }
    
    private String Decode(String str) {
        StringBuffer result = new StringBuffer();
        int l = str.length();
        for (int i = 0; i < l; ++i) {
            char c = str.charAt(i);
            if (c == '%' && i + 2 < l) {
                char c1 = str.charAt(i + 1);
                char c2 = str.charAt(i + 2);
                if (isHexit(c1) && isHexit(c2)) {
                    result.append((char)(hexit(c1) * 16 + hexit(c2)));
                    i += 2;
                } else
                    result.append(c);
            } else if (c == '+')
                result.append(' ');
            else
                result.append(c);
        }
        return result.toString();
   }

   private boolean isHexit( char c ) {
        String legalChars = "0123456789abcdefABCDEF";
        return (legalChars.indexOf(c) != -1 );
   }
   
   private int hexit( char c ) {
        if ( c >= '0' && c <= '9' )
            return c - '0';
        if ( c >= 'a' && c <= 'f' )
            return c - 'a' + 10;
        if ( c >= 'A' && c <= 'F' )
            return c - 'A' + 10;
        return 0;       // shouldn't happen, we're guarded by isHexit()
   }
}
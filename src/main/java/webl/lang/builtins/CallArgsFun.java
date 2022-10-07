package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;
import java.io.*;

public class CallArgsFun extends AbstractFunExpr
{
    public String toString() {
        return "<CallArgs>";
    }

    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        Expr t = ((Expr)(args.elementAt(0))).eval(c);
        if (t instanceof ListExpr) {
            ListExpr L = (ListExpr)t;
            
            int len = L.getSize();
            if (len < 1)
                throw new WebLException(c, callsite, "ArgumentError", 
                    toString() + " function expects a list containing at least one element as argument");

            String[] cmd = new String[len];
            int i = 0;
            while (!L.isEmpty()) {
                cmd[i++] = L.First().print();
                L = L.Rest();
            }
            
            try {
                Process P = Runtime.getRuntime().exec(cmd);
                
                StringBuffer res = new StringBuffer();
                Reader I = new BufferedReader(new InputStreamReader(P.getInputStream()));
                int chr = I.read();
                while (chr != -1) {
                    res.append((char)chr);
                    chr = I.read();
                }
                P.waitFor();
                return Program.Str(res.toString());
            } catch(InterruptedException e) {
                throw new WebLException(c, callsite, "InterruptedException", cmd + ": " + e.toString());
            } catch(IOException e) {
                throw new WebLException(c, callsite, "IOException", cmd + ": " + e.toString());
            }
        }
        throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a list as argument");
    }
}

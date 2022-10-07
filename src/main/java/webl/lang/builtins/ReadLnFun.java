package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;
import java.io.*;

public class ReadLnFun extends AbstractFunExpr
{
    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    public String toString() {
        return "<ReadLn>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 0);

        try {
            String line = in.readLine();
            if (line == null)
                return Program.nilval;
            else
                return Program.Str(line);
        } catch (IOException ie) {
            throw new WebLException(c, callsite, "IOException", "IOException while reading a line from input");
        }
    }

}
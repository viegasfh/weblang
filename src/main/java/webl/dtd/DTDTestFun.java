package webl.dtd;

import webl.lang.*;
import webl.lang.expr.*;
import webl.util.*;

import java.util.*;
import java.io.*;

public class DTDTestFun extends AbstractFunExpr
{
    public String toString() {
        return "<DTDTest>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        
        String file = StringArg(c, args, callsite, 0);
        
        try {
            Reader R = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            DTDParser P = new DTDParser(R);
            webl.util.Timer T = new webl.util.Timer();
            T.Start();
            DTD dtd = P.Parse();
            T.Report("parse complete");
        } catch (Exception e) {
            Log.println(e.toString());
        }
        return Program.nilval;
    }
}

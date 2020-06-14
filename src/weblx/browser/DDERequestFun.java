package weblx.browser;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class DDERequestFun extends AbstractFunExpr
{
    static private Win32 win32 = new Win32();
    
    public String toString() {
        return "<Browser_DDERequest>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 3);

        String app = StringArg(c, args, callsite, 0);
        String topic = StringArg(c, args, callsite, 1);
        String item = StringArg(c, args, callsite, 2);
        try {
            return Program.Str(win32.DDERequest(app, topic, item));
        } catch (IllegalArgumentException e) {
            throw new WebLException(c, callsite, "DDEError", e.toString());
        }
    }
}

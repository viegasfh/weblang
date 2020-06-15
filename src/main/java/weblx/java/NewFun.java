package weblx.java;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class NewFun extends AbstractFunExpr
{
    public String toString() {
        return "<Java_New>";
    }

    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        if (args.size() < 1)
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects 1 or more arguments");
            
        String classname = StringArg(c, args, callsite, 0);
        
        try {
            ClassDesc D = ClassDesc.Get(Class.forName(classname));
            
            // Evaluate all the arguments
            Vector R = new Vector();
            for (int i = 1; i < args.size(); i++) {
                Expr e = (Expr)(args.elementAt(i));
                R.addElement(e.eval(c));
            }
            return D.New(c, callsite, R);
        } catch (NoClassDefFoundError e) {
            throw new WebLException(c, callsite, "InstantiationError", "class not found, " + e.toString());
        } catch (NoSuchMethodError e) {
            throw new WebLException(c, callsite, "InstantiationError", "no such method, " + e.toString());
        } catch (ClassNotFoundException e) {
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a valid java class name as argument");
        }
    }
}
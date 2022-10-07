package weblx.java;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class ClassFun extends AbstractFunExpr
{
    public String toString() {
        return "<Java_Class>";
    }

    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        if (args.size() != 1)
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a class name as argument");
            
        String classname = StringArg(c, args, callsite, 0);
        try {
            return new JavaClassExpr(Class.forName(classname));
        } catch (NoClassDefFoundError e) {
            throw new WebLException(c, callsite, "InstantiationError", "class not found, " + e.toString());
        } catch (NoSuchMethodError e) {
            throw new WebLException(c, callsite, "InstantiationError", "no such method, " + e.toString());
        } catch (ClassNotFoundException e) {
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a valid java class name as argument");
        }
    }
}
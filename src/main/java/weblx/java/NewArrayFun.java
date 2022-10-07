package weblx.java;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;

public class NewArrayFun extends AbstractFunExpr
{
    public String toString() {
        return "<Java_NewArray>";
    }

    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        
        String classname = StringArg(c, args, callsite, 0);
        long size = IntArg(c, args, callsite, 1);
        
        try {
            Class clss;
            if (classname.equals("boolean"))
                clss = boolean.class;
             else if (classname.equals("byte"))
                clss = byte.class;
             else if (classname.equals("char"))
                clss = char.class;
             else if (classname.equals("double"))
                clss = double.class;
             else if (classname.equals("float"))
                clss = float.class;
             else if (classname.equals("int"))
                clss = int.class;
             else if (classname.equals("long"))
                clss = long.class;
             else if (classname.equals("short"))
                clss = short.class;
             else
                clss = Class.forName(classname);
                
            return new JavaArrayExpr(clss, (int)size);
        } catch (NegativeArraySizeException e) {
            throw new WebLException(c, callsite, "NegativeArraySizeError", "negative array size");
        } catch (ClassNotFoundException e) {
            throw new WebLException(c, callsite, "ClassNotFoundError", "no such class");
        }
    }
}
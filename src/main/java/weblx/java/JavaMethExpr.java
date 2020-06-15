package weblx.java;

import java.util.*;
import java.lang.reflect.*;
import webl.lang.*;
import webl.lang.expr.*;

public class JavaMethExpr extends AbstractMethExpr
{
    public String   name;
    public Class    clss;
    public Vector   meth;
    public Vector   param = new Vector();
    public Vector   ret = new Vector();
    
    public JavaMethExpr(String name, Class clss, Vector meth) {
        super(-1);
        this.name = name;
        this.clss = clss;
        this.meth = meth;
        
        for(int i = 0; i < meth.size(); i++) {
            Method m = (Method)meth.elementAt(i);
            param.addElement(m.getParameterTypes());
            ret.addElement(m.getReturnType());
        }
    }

    public Expr Apply(Context cc, Expr self, Vector arg, Expr callsite) throws WebLException {
        Object selfobj = null;
        
        if (self instanceof JavaObjectExpr) {
            selfobj = ((JavaObjectExpr)self).obj;
            if (selfobj.getClass() != clss)
                throw new WebLException(cc, callsite, "MethodMismatch", "method is not being applied against the correct Java object");
        } else if (self instanceof JavaClassExpr) {
            selfobj = null;
        } else
            throw new WebLException(cc, callsite, "NotAJavaObject", "method is not being applied against a Java object");

        // Evaluate all the arguments
        Vector R = new Vector();
        for (int i = 0; i < arg.size(); i++) {
            Expr e = (Expr)(arg.elementAt(i));
            R.addElement(e.eval(cc));
        }
        
        int bestmethod = -1;
        int bestdistance = 1000;
        for(int i = 0; i < meth.size(); i++) {      // check out all methods
            int distance = ClassDesc.Distance(R, (Class[])param.elementAt(i));
            if (distance < bestdistance) {
                bestdistance = distance;
                bestmethod = i;
            }
        }
        
        if (bestmethod == -1) 
            throw new WebLException(cc, callsite, "NoApplicableJavaMethod", "no java method was found that matched the actual arguments of " + toString());
        Method m = (Method)meth.elementAt(bestmethod);
        
        Object[] margs = ClassDesc.Convert2Java(cc, callsite, R, (Class[])param.elementAt(bestmethod));
        try {
            Object result = m.invoke(selfobj, margs);
            if (result == null) return Program.nilval;
                
            Expr res = ClassDesc.Convert2WebL(result);            
            if (res == null) 
                throw new WebLException(cc, callsite, "ConversionError", "Could not convert return value of type " + result.getClass().getName() + " to a WebL type");
            return res;
        } catch (InvocationTargetException e) {
            Throwable E = e.getTargetException();
            throw new WebLException(cc, callsite, E.getClass().getName(), E.toString());
        } catch (IllegalAccessException e) {
            throw new WebLException(cc, callsite, "IllegalAccessError", "Illegal access exception");
        }
    }
    
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("<");
//        s.append(clss.getName()).append(": ");
        for(int i = 0; i < meth.size(); i++) {      // check out all methods
            s.append(name).append('(');
            Class[] par = (Class[])param.elementAt(i);
            for(int j = 0; j < par.length; j++) {
                s.append(par[j].getName());
                if (j < par.length - 1)
                    s.append(',');
            }
            s.append("): ");
            s.append(((Class)ret.elementAt(i)).getName());
            if (i < meth.size() - 1)
                s.append("; ");
        }
        s.append(">");
        return s.toString();
    }
    
}
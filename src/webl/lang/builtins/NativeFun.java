package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.util.*;
import java.util.*;
import java.io.*;

public class NativeFun extends AbstractFunExpr
{
    private static NativeFunLoader loader = new NativeFunLoader();
    
    public String toString() {
        return "<Native>";
    }
    
    public AbstractFunExpr loadFun(Context c, String name) throws WebLException {
        Class cl = loader.loadClass(name, true);
        if (cl == null)
            throw new WebLException(c, this, "NativeCodeImportError", "load of class " + name + " failed");
        
        Object obj;
        try {
            obj = cl.newInstance();
        } catch (InstantiationException e) {
            throw new WebLException(c, this, "NativeCodeImportError", "instantiation of class " + name + " failed");
        } catch (IllegalAccessException e) {
            throw new WebLException(c, this, "NativeCodeImportError", "access to class " + name + " failed");
        } catch (NoSuchMethodError e) {
            throw new WebLException(c, this, "NativeCodeImportError", "no such method of class " + name);
        }
        if (obj instanceof AbstractFunExpr)
            return (AbstractFunExpr)obj;
        else
            throw new WebLException(c, this, "NativeCodeImportError", "class " + name + " is not a subclass of webl.lang.builtin");
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 1);
        String name = StringArg(c, args, callsite, 0);
        return loadFun(c, name);
    }
}

class NativeFunLoader extends ClassLoader
{
    Hashtable cache = new Hashtable();

    private byte[] loadClassData(String name) throws IOException{ 
        InputStream  fp = FileLocator.Find(name + ".class");
        if(fp != null){
            int n = fp.available();
            byte res[] = new byte[n];
            fp.read(res,0,n);
            fp.close();
            return res;
        } else
            return null;
    }

    public synchronized Class loadClass(String name, boolean resolve) {
        Class c = (Class)cache.get(name);
        if (c == null) {
            try {
                c = (Class)Class.forName(name);
            } catch (ClassNotFoundException ex) {
                try {
                    byte data[] = loadClassData(name);
                    if (data == null) return null;
                    c = defineClass(name, data, 0, data.length);
                    cache.put(name, c);
                } catch(IOException ex2) {
                    return null;
                }
            }
        }
        if (resolve)
            resolveClass(c);
        return c;
    }
}



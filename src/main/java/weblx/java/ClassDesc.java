package weblx.java;

import java.lang.reflect.*;
import java.util.*;
import webl.lang.*;
import webl.lang.expr.*;
import webl.util.Log;

public class ClassDesc
{
    public Class               clss;
    public Hashtable           members = new Hashtable();
    public Constr[]            constructor;
    
    static public Integer      Illegal = new Integer(42);
    
    static private Hashtable   cache = new Hashtable();
    
    static public ClassDesc Get(Class clss) {
        synchronized(cache) {
            ClassDesc R = (ClassDesc)cache.get(clss);
            if (R == null) {
                R = new ClassDesc(clss);
                cache.put(clss, R);
            }
            return R;
        }
    }

    public boolean empty() {
        return members.size() == 0;
    }
    
    protected ClassDesc(Class clss) {
        Log.debugln("[Importing java class " + clss.getName() + "]");
        
        this.clss = clss;
        LearnConstructors(clss);
        LearnMethods(clss);
        LearnFields(clss);
    }
    
    protected void LearnConstructors(Class clss) {
        Constructor[] C = clss.getConstructors();
        constructor = new Constr[C.length];
        for (int i = 0; i < C.length; i++) 
            constructor[i] = new Constr(C[i]);
    }
    
    protected void LearnFields(Class clss) {
        Field[] F = clss.getFields();
        for (int i = 0; i < F.length; i++) {
            Field f = F[i];
            members.put(Program.Str(f.getName()), f);
        }
    }
    
    protected void LearnMethods(Class clss) {
        Hashtable nhash = new Hashtable();
        
        // Learn all the methods
        Method[] meth = clss.getMethods();
        for (int i = 0; i < meth.length; i++) {
            Method m = meth[i];
            String name = m.getName();
            
            Vector V = (Vector)nhash.get(name);
            if (V == null) {
                V = new Vector();
                nhash.put(name, V);
            }
            V.addElement(m);
        }
        
        Enumeration enum = nhash.keys();
        while (enum.hasMoreElements()) {
            String name = (String)enum.nextElement();
            Vector V = (Vector)nhash.get(name);
            JavaMethExpr m = new JavaMethExpr(name, clss, V);
            members.put(Program.Str(name), m);
        }        
    }
    
    public Enumeration EnumKeys() {
        return members.keys();
    }
    
    public Expr getField(Object obj, Expr key) {
        Object v = members.get(key);
        if (v == null) return null;
        
        if (v instanceof Field) {
            try {
                Field f = (Field)v;
                return Convert2WebL(f.get(obj));
            } catch (IllegalAccessException e) {
                return null;
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return (Expr)v;
    }
    
    public boolean setField(Object obj, Expr key, Expr val) {
        Object v = members.get(key);
        if (v == null) return false;
        
        if (v instanceof Field) {
            try {
                Field f = (Field)v;
                Object jval = Convert2Java(val, f.getType());
                if (jval == Illegal)
                    return false;
                f.set(obj, jval);
                return true;
            } catch (IllegalAccessException e) {
                return false;
            } catch (IllegalArgumentException e) {
                return false;
            }
        } else
            return false;        
    }
    
    public Expr New(Context cc, Expr callsite, Vector arg) throws WebLException {
        int bestconstructor = -1;
        int bestdistance = 1000;
        for(int i = 0; i < constructor.length; i++) {      // check out all methods
            int distance = Distance(arg, constructor[i].param);
            if (distance < bestdistance) {
                bestdistance = distance;
                bestconstructor = i;
            }
        }
        
        if (bestconstructor == -1) 
            throw new WebLException(cc, callsite, "NoApplicableJavaConstructor", "no java constructor was found that matched the actual arguments");

        Constr C = constructor[bestconstructor];
        
        Object[] cargs = Convert2Java(cc, callsite, arg, C.param);
        try {
            Object result = C.constructor.newInstance(cargs);
            if (result == null)
                return Program.nilval;
                
            Expr res = Convert2WebL(result);            
            if (res == null) 
                throw new WebLException(cc, callsite, "ConversionError", "Could not convert return value of type " + result.getClass().getName() + " to a WebL type");
            return res;
        } catch (InstantiationException e) {
            throw new WebLException(cc, callsite, "InstantiationError", "object could not be instantiated");
        } catch (InvocationTargetException e) {
            Throwable E = e.getTargetException();
            throw new WebLException(cc, callsite, E.getClass().getName(), E.toString());            
        } catch (IllegalAccessException e) {
            throw new WebLException(cc, callsite, "IllegalAccessError", "Illegal access exception");
        }        
    }
    
    static public int Distance(Vector arg, Class[] param) {
        if (param.length != arg.size())         // wrong number of args
            return 1000;
        
        int distance = 0;
        for (int j = 0; j < arg.size(); j++) {  // argument distance calculation
            Expr e = (Expr)(arg.elementAt(j));
            Class c = param[j];
            distance += Distance(e, c);
        }
        return distance;
    }
    
    static public int Distance(Expr e, Class c) {
        if (e instanceof BooleanExpr) {
            if (c == boolean.class) return 0;
            else
                return 1000;
        } else if (e instanceof NilExpr) {
            if (c.isPrimitive()) return 1000;
            else
                return 0;
        } else if (e instanceof CharExpr) {
            if (c == char.class) return 0;
            else if (c == String.class) return 1;
            else
                return 1000;
        } else if (e instanceof StringExpr) {
            if (c.isAssignableFrom(String.class)) return 0;
            else
                return 1000;
        } else if (e instanceof IntExpr) {
            if (c == int.class) return 1;
            else if (c == long.class) return 0;
            else if (c == short.class) return 2;
            else if (c == byte.class) return 3;
            else if (c == float.class) return 5;
            else if (c == double.class) return 4;
            else
                return 1000;
        } else if (e instanceof RealExpr) {
            if (c == float.class) return 1;
            else if (c == double.class) return 0;
            else
                return 1000;
        } else if (e instanceof JavaObjectExpr) {
            Object o = ((JavaObjectExpr)e).obj;
            if (c.isAssignableFrom(o.getClass())) return 0;
            else
                return 1000;
        } else if (e instanceof JavaArrayExpr) {
            if (c.isArray())
                return 0;
            else
                return 1000;
        } else if (c.isAssignableFrom(e.getClass())) {
            return 0;
        } else 
            return 1000;
    }    
    
    static public Object[] Convert2Java(Context cc, Expr callsite, Vector arg, Class[] par) throws WebLException {
        Object[] cargs = new Object[arg.size()];
        for (int j = 0; j < arg.size(); j++) { 
            Expr e = (Expr)(arg.elementAt(j));
            cargs[j] = Convert2Java(e, par[j]);
            if (cargs[j] == Illegal) 
                throw new WebLException(cc, callsite, "ConversionError", "Could not convert " + e + " to " + par[j].getName());
        }
        return cargs;
    }
    
    static public Object Convert2Java(Expr e, Class c) {
        if (e instanceof BooleanExpr) {
            if (c == boolean.class)
                return new Boolean(((BooleanExpr)e).val);
            else
                return Illegal;
        } else if (e instanceof NilExpr) {
            return null;
        } else if (e instanceof CharExpr) {
            if (c == char.class)
                return new Character(((CharExpr)e).ch);
            else if (c == String.class) {
                char ch = ((CharExpr)e).ch;
                return String.valueOf(ch);
            } else
                return Illegal;
        } else if (e instanceof StringExpr) {
            return ((StringExpr)e).val();
        } else if (e instanceof IntExpr) {
            long val = ((IntExpr)e).val;
            if (c == int.class) return new Integer((int)val);
            else if (c == long.class) return new Long(val);
            else if (c == short.class) return new Short((short)val);
            else if (c == byte.class) return new Byte((byte)val);
            else if (c == float.class) return new Float((float)val);
            else if (c == double.class) return new Double((double)val);
            else
                return Illegal;
        } else if (e instanceof RealExpr) {
            double val = ((RealExpr)e).val;
            if (c == float.class) return new Float(val);
            else if (c == double.class) return new Double(val);
            else
                return Illegal;
        } else if (e instanceof JavaObjectExpr) {
            return ((JavaObjectExpr)e).obj;
        } else if (e instanceof JavaArrayExpr) {
            return ((JavaArrayExpr)e).array;
        } else 
            return e;
    }
    
    static public Expr Convert2WebL(Object o) {
        if (o == null) {
            return Program.nilval;
        } else if (o instanceof Expr) {
            return (Expr)o;
        } else if (o instanceof Boolean) {
            if (((Boolean)o).booleanValue())
                return Program.trueval;
            else
                return Program.falseval;
        } else if (o instanceof Character) {
            return Program.Chr(((Character)o).charValue());
        } else if (o instanceof String) {
            return Program.Str((String)o);
        } else if (o instanceof Long) {
            return Program.Int(((Long)o).longValue());
        } else if (o instanceof Integer) {
            return Program.Int(((Integer)o).longValue());
        } else if (o instanceof Short) {
            return Program.Int(((Short)o).longValue());
        } else if (o instanceof Byte) {
            return Program.Int(((Byte)o).longValue());
        } else if (o instanceof Float) {
            return Program.Real(((Float)o).doubleValue());
        } else if (o instanceof Double) {
            return Program.Real(((Double)o).doubleValue());
        } else if (o instanceof Void) {
            return Program.nilval;
        } else if (o.getClass().isArray()) {
            return new JavaArrayExpr(o);
        } else
            return new JavaObjectExpr(o);
    }
}

class Constr
{
    public Constructor      constructor;
    public Class[]          param;
    
    public Constr(Constructor C) {
        constructor = C;
        param = C.getParameterTypes();  
    }
}

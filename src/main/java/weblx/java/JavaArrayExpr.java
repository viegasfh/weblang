package weblx.java;

import webl.lang.*;
import webl.lang.expr.*;
import java.util.*;
import java.lang.reflect.*;

public class JavaArrayExpr extends ValueExpr 
{
    public Class  base;
    public Object array;
    
    public String getTypeName() {
        return "j-array";
    }
    
    public JavaArrayExpr(Class base, int size) throws NegativeArraySizeException {
        super(0);
        this.base = base;
        array = Array.newInstance(base, size);
    }

    public String toString() {
        return "<java array of type " + base.getName() + "[]>";
    }
    
    public JavaArrayExpr(Object array) {
        super(0);
        base = array.getClass().getComponentType();
        this.array = array;
    }
    
    public int size() {
        return Array.getLength(array);
    }
    
    public Expr get(int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (base == boolean.class) {
            if (Array.getBoolean(array, index))
                return Program.trueval;
            else
                return Program.falseval;
        } else if (base == byte.class) {
            return Program.Int(Array.getByte(array, index));
        } else if (base == char.class) {
            return Program.Chr(Array.getChar(array, index));
        } else if (base == double.class) {
            return Program.Real(Array.getDouble(array, index));
        } else if (base == float.class) {
            return Program.Real(Array.getFloat(array, index));
        } else if (base == int.class) {
            return Program.Int(Array.getInt(array, index));
        } else if (base == long.class) {
            return Program.Int(Array.getLong(array, index));
        } else if (base == short.class) {
            return Program.Int(Array.getShort(array, index));
        } else {
            Object o = Array.get(array, index);
            return ClassDesc.Convert2WebL(o);
        }
    }
    
    public boolean set(int index, Expr e)  throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (e instanceof BooleanExpr) {
            if (base == boolean.class) {
                Array.setBoolean(array, index, ((BooleanExpr)e).val);
                return true;
            } else
                return false;
        } else if (e instanceof StringExpr) {
            Array.set(array, index, ((StringExpr)e).val());
            return true;
        } else if (e instanceof NilExpr) {
            Array.set(array, index, null);
            return true;
        } else if (e instanceof CharExpr) {
            if (base == char.class) {
                Array.setChar(array, index, ((CharExpr)e).ch);
                return true;
            } else if (base == String.class) {
                char ch = ((CharExpr)e).ch;
                Array.set(array, index, String.valueOf(ch));
                return true;
            } else
                return false;
        } else if (e instanceof IntExpr) {
            long val = ((IntExpr)e).val;
            if (base == int.class) {
                Array.setInt(array, index, (int)val);
                return true;
            } else if (base == long.class) {
                Array.setLong(array, index, val);
                return true;
            } else if (base == short.class) {
                Array.setShort(array, index, (short)val);
                return true;
            } else if (base == byte.class) {
                Array.setByte(array, index, (byte)val);
                return true;
            } else if (base == float.class) {
                Array.setFloat(array, index, (float)val);
                return true;
            } else if (base == double.class) {
                Array.setDouble(array, index, (double)val);
                return true;
            } else
                return false;
        } else if (e instanceof RealExpr) {
            double val = ((RealExpr)e).val;
            if (base == float.class) {
                Array.setFloat(array, index, (float)val);
                return true;
            } else if (base == double.class) {
                Array.setDouble(array, index, val);
                return true;
            } else
                return false;
        } else if (e instanceof JavaObjectExpr) {
            Array.set(array, index, ((JavaObjectExpr)e).obj);
            return true;
        } else if (e instanceof JavaArrayExpr) {
            Array.set(array, index, ((JavaArrayExpr)e).array);
            return true;
        } else {
            Array.set(array, index, e);
            return true;        
        }
    }
}
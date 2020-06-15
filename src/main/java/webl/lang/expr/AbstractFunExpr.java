package webl.lang.expr;

import webl.lang.*;
import java.util.*;

abstract public class AbstractFunExpr extends ValueExpr
{
    public AbstractFunExpr(int ppos) {
        super(ppos);
    }   
   
    public AbstractFunExpr() {
        super(-1);
    }   
    
    public String getTypeName() {
        return "fun";
    }
    
    abstract public String toString();
    
    abstract public Expr Apply(Context c, Vector cargs, Expr callsite) throws WebLException;

    protected void CheckArgCount(Context c, Vector args, Expr callsite, int count) throws WebLException {
        if (args.size() != count)
            throw new WebLException(c, callsite, "ArgumentError", "wrong number of arguments, " 
            + this + " function expects " + count + " argument(s)");
       
    }
    
    protected String StringArg(Context c, Vector args, Expr callsite, int param) throws WebLException {
        Expr r = ((Expr)(args.elementAt(param))).eval(c);
        if (r instanceof StringExpr)
            return ((StringExpr)r).val();
        else
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a string as argument " + param);
    }
    
    protected long IntArg(Context c, Vector args, Expr callsite, int param) throws WebLException {
        Expr r = ((Expr)(args.elementAt(param))).eval(c);
        if (r instanceof IntExpr)
            return ((IntExpr)r).val;
        else
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects an integer as argument " + param);
    }
    
    protected double RealArg(Context c, Vector args, Expr callsite, int param) throws WebLException {
        Expr r = ((Expr)(args.elementAt(param))).eval(c);
        if (r instanceof RealExpr)
            return ((RealExpr)r).val;
        else
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a real as argument " + param);
    }
    
    protected char CharArg(Context c, Vector args, Expr callsite, int param) throws WebLException {
        Expr r = ((Expr)(args.elementAt(param))).eval(c);
        if (r instanceof CharExpr)
            return ((CharExpr)r).ch;
        else
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects an character as argument " + param);
    }
    
    protected boolean BoolArg(Context c, Vector args, Expr callsite, int param) throws WebLException {
        Expr r = ((Expr)(args.elementAt(param))).eval(c);
        if (r instanceof BooleanExpr)
            return r == Program.trueval;
        else
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a boolean as argument " + param);
    }
    
    protected ObjectExpr ObjectArg(Context c, Vector args, Expr callsite, int param) throws WebLException {
        Expr r = ((Expr)(args.elementAt(param))).eval(c);
        if (r instanceof ObjectExpr)
            return (ObjectExpr)r;
        else
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects an object as argument " + param);
    }    
    
    protected SetExpr SetArg(Context c, Vector args, Expr callsite, int param) throws WebLException {
        Expr r = ((Expr)(args.elementAt(param))).eval(c);
        if (r instanceof SetExpr)
            return (SetExpr)r;
        else
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a set as argument " + param);
    }      
}

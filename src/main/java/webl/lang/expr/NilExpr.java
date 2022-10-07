package webl.lang.expr;

import webl.lang.*;

public class NilExpr extends ObjectExpr
{
    public NilExpr() {
        super(0);
    }
    
    public String getTypeName() {
        return "nil";
    }
    
    public synchronized String toString() {
        return "nil";
    }

    public synchronized int size() {
        return 0;
    }

    public synchronized Expr get(Expr key) {
        return null;
    }

    public synchronized boolean def(Expr key, Expr val) {
        return false;
    }

    public synchronized boolean set(Expr key, Expr val) {
        return false;
    }
}


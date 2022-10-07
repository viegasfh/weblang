package webl.lang.expr;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;

public class IndexExpr extends Expr
{
    public Expr obj, index;

    public IndexExpr(Expr obj, Expr index, int ppos) {
        super(ppos);
        this.obj = obj; this.index = index;
    }

    public String toString() {
        return obj + "[" + index + "]";
    }

    public Expr eval(Context c) throws WebLException {
        Expr e = obj.eval(c);
        if (e instanceof ListExpr) {
            Expr key = index.eval(c);
            if (key instanceof IntExpr) {
                try {
                    int x = (int)((IntExpr)key).val;
                    return ((ListExpr)e).getElementAt(x);
                } catch(IndexOutOfBoundsException i) {
                    throw new WebLException(c, this, "IndexRangeError", "index out of list bounds");
                }
            } else
                throw new WebLException(c, this, "ArgumentError", "lists can only be indexed with integers"); 
        } else if (e instanceof ObjectExpr) {  
            Expr key = index.eval(c);
            Expr v = ((ObjectExpr) e).get(key);
            if(v == null)
                throw new WebLException(c, this, "NoSuchField", "object does not contain field " + key.toString());
            else
                return v;
        } else if (e instanceof StringExpr) {
            Expr key = index.eval(c);
            if (key instanceof IntExpr) {
                int x = (int)((IntExpr)key).val;
                try {
                    return Program.Chr(((StringExpr)e).val().charAt(x));
                } catch(StringIndexOutOfBoundsException d) {
                    throw new WebLException(c, this, "IndexRangeError", "index out of string bounds");
                }
            } else
                throw new WebLException(c, this, "ArgumentError", "strings can only be indexed with integers");
        } else if (e instanceof PieceSet) {
            Expr key = index.eval(c);
            if (key instanceof IntExpr) {
                int x = (int)((IntExpr)key).val;
                try {
                    return PieceSet.OpIndex((PieceSet)e, x);
                } catch(IndexOutOfBoundsException d) {
                    throw new WebLException(c, this, "IndexRangeError", "index out of pieceset bounds");
                }
            } else
                throw new WebLException(c, this, "ArgumentError", "piece-sets can only be indexed with integers");
        
        } else
            throw new WebLException(c, this, "NotAnObject", "not an object");
    }
}
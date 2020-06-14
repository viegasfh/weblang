package webl.lang.expr;

import webl.lang.*;
import webl.page.*;

public class PlusExpr extends Expr
{
    public Expr x, y;

    public PlusExpr(Expr x, Expr y, int ppos) {
        super(ppos);
        this.x = x; this.y = y;
    }

    public String toString() {
        return "(" + x + " + " + y + ")";
    }

    public Expr eval(Context c) throws WebLException {
        Expr x0 = x.eval(c);
        Expr y0 = y.eval(c);
        if (x0 instanceof IntExpr) {
            if (y0 instanceof IntExpr)
                return new IntExpr(((IntExpr)x0).val + ((IntExpr)y0).val);
            else if (y0 instanceof RealExpr)
                return new RealExpr(((IntExpr)x0).val + ((RealExpr)y0).val);
        } else if (x0 instanceof RealExpr) {
            if (y0 instanceof RealExpr)
                return new RealExpr(((RealExpr)x0).val + ((RealExpr)y0).val);
            else if (y0 instanceof IntExpr)
                return new RealExpr(((RealExpr)x0).val + ((IntExpr)y0).val);
        } else if (x0 instanceof StringExpr) {
            if (y0 instanceof StringExpr)
                return StringExpr.Concat((StringExpr)x0, (StringExpr)y0);
            else if (y0 instanceof CharExpr) 
                return StringExpr.Concat((StringExpr)x0, (CharExpr)y0);
        } else if (x0 instanceof CharExpr) {
            if (y0 instanceof StringExpr)
                return StringExpr.Concat((CharExpr)x0, (StringExpr)y0);
            else if (y0 instanceof CharExpr) {
                return StringExpr.Concat((CharExpr)x0, (CharExpr)y0);
            }
        } else if (x0 instanceof SetExpr) {
            if (y0 instanceof SetExpr) {
                return ((SetExpr)x0).Union((SetExpr)y0);
            }
        } else if (x0 instanceof ListExpr) {
            if (y0 instanceof ListExpr) {
                return ((ListExpr)x0).appendList((ListExpr)y0);
            }
        } else {
            try {
                PieceSet x1 = PieceSet.castExpr(x0);
                PieceSet y1 = PieceSet.castExpr(y0);
                if (x1 != null && y1 != null)
                    return PieceSet.OpUnion(x1, y1);
            } catch (TypeCheckException e) {
                throw new WebLException(c, this, "OperandMismatch", "incompatible operands for + operator");
            }
        }
        throw new WebLException(c, this, "OperandMismatch", "incompatible operands for + operator");
    }
}
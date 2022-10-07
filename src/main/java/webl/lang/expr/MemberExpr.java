package webl.lang.expr;

import webl.lang.*;
import webl.page.*;

public class MemberExpr extends Expr
{
    public Expr x, y;

    public MemberExpr(Expr x, Expr y, int ppos) {
        super(ppos);
        this.x = x; this.y = y;
    }

    public String toString() {
        return "(" + x + " member " + y + ")";
    }

    public Expr eval(Context c) throws WebLException {
        Expr x0 = x.eval(c);
        Expr y0 = y.eval(c);
        if (y0 instanceof SetExpr) {
            if(((SetExpr)y0).Contain(x0))
                return Program.trueval;
            else
                return Program.falseval;
        } else if (y0 instanceof ListExpr) {
            if(((ListExpr)y0).contains(x0))
                return Program.trueval;
            else
                return Program.falseval;
        } else if (y0 instanceof ObjectExpr) {
            if(((ObjectExpr)y0).get(x0) != null)
                return Program.trueval;
            else
                return Program.falseval;
        }  else if (y0 instanceof PieceSet) {
            if (x0 instanceof Piece && ((PieceSet)y0).member((Piece)x0))
                return Program.trueval;
            else
                return Program.falseval;
        }
            
        throw new WebLException(c, this, "OperandMismatch", "incompatible operands for member operator");
    }
}
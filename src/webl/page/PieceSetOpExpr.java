package webl.page;

import webl.lang.*;
import webl.lang.expr.*;

public class PieceSetOpExpr extends Expr
{
    int op;
    public Expr x, y;

    public PieceSetOpExpr(Expr x, int op, Expr y, int ppos) {
        super(ppos);
        this.x = x; this.y = y;
        this.op = op;
    }

    public String toString() {
        return "(" + x + " " + Scanner.name(op) + " " + y + ")";
    }
    
    private PieceSet getArg(Context c, Expr x) throws WebLException {
        Expr x0 = x.eval(c);
        if (x0 instanceof PieceSet)
            return (PieceSet)x0;
        else if (x0 instanceof Piece) 
            return PieceSet.make((Piece)x0);
        else
            throw new WebLException(c, this, "OperandMismatch", "incompatible operands for operator");
    }

    public Expr eval(Context c) throws WebLException {
        PieceSet xs = getArg(c, x);
        PieceSet ys = getArg(c, y);
        
        try {
            switch(op) {
                case Scanner.INSIDE:
                    return PieceSet.OpInside(xs, ys, false);
                case Scanner.NOTINSIDE:
                    return PieceSet.OpInside(xs, ys, true);
                case Scanner.DIRECTLYINSIDE:
                    return PieceSet.OpDirectlyInside(xs, ys, false);
                case Scanner.NOTDIRECTLYINSIDE:
                    return PieceSet.OpDirectlyInside(xs, ys, true);
                case Scanner.CONTAIN:
                    return PieceSet.OpContain(xs, ys, false);
                case Scanner.NOTCONTAIN:
                    return PieceSet.OpContain(xs, ys, true);
                case Scanner.DIRECTLYCONTAIN:
                    return PieceSet.OpDirectlyContain(xs, ys, false);
                case Scanner.NOTDIRECTLYCONTAIN:
                    return PieceSet.OpDirectlyContain(xs, ys, true);
                case Scanner.AFTER:
                    return PieceSet.OpAfter(xs, ys, false);
                case Scanner.NOTAFTER:
                    return PieceSet.OpAfter(xs, ys, true);
                case Scanner.DIRECTLYAFTER:
                    return PieceSet.OpDirectlyAfter(xs, ys, false);
                case Scanner.NOTDIRECTLYAFTER:
                    return PieceSet.OpDirectlyAfter(xs, ys, true);
                case Scanner.BEFORE:
                    return PieceSet.OpBefore(xs, ys, false);
                case Scanner.NOTBEFORE:
                    return PieceSet.OpBefore(xs, ys, true);
                case Scanner.DIRECTLYBEFORE:
                    return PieceSet.OpDirectlyBefore(xs, ys, false);
                case Scanner.NOTDIRECTLYBEFORE:
                    return PieceSet.OpDirectlyBefore(xs, ys, true);
                case Scanner.OVERLAP:
                    return PieceSet.OpOverlap(xs, ys, false);
                case Scanner.NOTOVERLAP:
                    return PieceSet.OpOverlap(xs, ys, true);
                case Scanner.WITHOUT:
                   return PieceSet.OpWithout(xs, ys);
                case Scanner.INTERSECT:
                   return PieceSet.OpRegionIntersect(xs, ys);
            } 
            throw new Error("internal error");
        } catch (TypeCheckException e) {
            throw new WebLException(c, this, "OperandMismatch", "incompatible operands for operator");
        }
    }
}

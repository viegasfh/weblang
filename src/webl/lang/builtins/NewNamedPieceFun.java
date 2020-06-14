package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;
import java.io.*;

public class NewNamedPieceFun extends AbstractFunExpr
{
    public String toString() {
        return "<NewNamedPiece>";
    }

    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        if (args.size() == 2) {
            String name = StringArg(c, args, callsite, 0);
            Expr r = ((Expr)(args.elementAt(1))).eval(c);
            if (r instanceof Piece) {
                Piece p = (Piece)r;
                return p.page.SpecialPiece(name, p.beg, p.end);
            }
        } else if (args.size() == 3) {
            String name = StringArg(c, args, callsite, 0);
            Expr r = ((Expr)(args.elementAt(1))).eval(c);
            if (r instanceof TagExpr) {
                TagExpr x =(TagExpr)r;
                r = ((Expr)(args.elementAt(2))).eval(c);
                if (r instanceof TagExpr) {
                    TagExpr y =(TagExpr)r;
                    if (x.page != y.page)
                        throw new WebLException(c, this, "NotSamePage", "the tag arguments to the " + this + " function do not belong to the same page");
                    return x.page.SpecialPiece(name, x.tag, y.tag);
                }
            }
        }
        throw new WebLException(c, this, "ArgumentError", this + " function expects (name: string, beg: tag, end: tag) as arguments");
    }
}
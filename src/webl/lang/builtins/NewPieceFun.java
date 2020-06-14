package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import webl.page.net.*;
import java.util.*;
import java.io.*;

public class NewPieceFun extends AbstractFunExpr
{
    public String toString() {
        return "<NewPiece>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        if (args.size() == 1) { // piece
            Expr r = ((Expr)(args.elementAt(0))).eval(c);
            if (r instanceof Piece) {
                Piece p = (Piece)r;
                return p.page.SpecialPiece(p.beg, p.end);
            }
        } else if (args.size() == 2) {  // str x str or tag x tag
            Expr r = ((Expr)(args.elementAt(0))).eval(c);
            if (r instanceof StringExpr) {
                String s = ((StringExpr)r).val();
                String mime = StringArg(c, args, callsite, 1);
                try {
                    Reader R = new BufferedReader(new StringReader(s));
            	    Page p = Net.FetchPage(R, "", mime, null);
            	    return p.getContentPiece();
            	} catch (NetException e) {
            	    throw e.makeWebLException(c, callsite, e);
            	}                
            } else if (r instanceof TagExpr) {
                TagExpr x =(TagExpr)r;
                r = ((Expr)(args.elementAt(1))).eval(c);
                if (r instanceof TagExpr) {
                    TagExpr y =(TagExpr)r;
                    if (x.page != y.page)
                        throw new WebLException(c, this, "NotSamePage", "the tag arguments to the " + this + " function do not belong to the same page");
                    return x.page.SpecialPiece(x.tag, y.tag);
                }
            }
        }
        throw new WebLException(c, this, "ArgumentError", this + " function expects (s: string, mime: string), (beg: tag, end: tag), or (p: piece) as arguments");

    }
}
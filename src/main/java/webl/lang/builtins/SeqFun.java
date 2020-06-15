package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;
import com.oroinc.text.regex.*;

public class SeqFun extends AbstractFunExpr
{
    public String toString() {
        return "<Seq>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        
        Expr p = ((Expr)(args.elementAt(0))).eval(c);   
        String s = StringArg(c, args, callsite, 1);
        if (p instanceof Page) {
            return ((Page)p).FindSeq(s);
        } else if (p instanceof Piece) {
            Piece P = (Piece)p;
            return P.page.FindSeq(P, s);
        } else
            throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a page or piece as first argument");
    }
}
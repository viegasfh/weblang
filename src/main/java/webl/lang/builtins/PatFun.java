package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;
import java.util.regex.*;

public class PatFun extends AbstractFunExpr
{
    public String toString() {
        return "<Pat>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        
        Expr p = ((Expr)(args.elementAt(0))).eval(c);   
        String s = StringArg(c, args, callsite, 1);
        try {
            if (p instanceof Page) {
                return ((Page)p).getPattern(s);
            } else if (p instanceof Piece) {
                return ((Piece)p).page.getPattern((Piece)p, s);
            } else
                throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a page or piece as first argument");
        } catch(PatternSyntaxException e) {
            throw new WebLException(c, callsite, "MalFormedPattern", "illegal regular expression passed to " + this + " function");
        } catch (TypeCheckException e) { // never happens
            throw new Error("internal error");
        }
    }
}
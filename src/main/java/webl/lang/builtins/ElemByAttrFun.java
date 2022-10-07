package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;

public class ElemByAttrFun extends AbstractFunExpr
{
    public String toString() {
        return "<ElemByAttr>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        if (args.size() < 3 || args.size() > 4)
            throw new WebLException(c, callsite, "ArgumentError", "wrong number of arguments, " 
            + this + " function expects three or four argument(s)");
        Expr p = null;
        String pieceName = "";
        String attrName = "";
        String attrValue = "";

        try {
            if (args.size() == 4) {
                p = ((Expr)(args.elementAt(0))).eval(c);   
                pieceName = StringArg(c, args, callsite, 1); // the piece name
                attrName = StringArg(c, args, callsite, 2); // the attribute name
                attrValue = StringArg(c, args, callsite, 3); // the attribute value
            } else {
                p = ((Expr)(args.elementAt(0))).eval(c);   
                attrName = StringArg(c, args, callsite, 1); // the attribute name
                attrValue = StringArg(c, args, callsite, 2); // the attribute value
            }

            if (p instanceof Page) {
                if (args.size() == 4)
                    return ((Page)p).getElemByAttr(pieceName, attrName, attrValue);
                else
                    return ((Page)p).getElemByAttr(attrName, attrValue); // return by attribute value
            } else if (p instanceof Piece) {
                if (args.size() == 4)
                    return ((Piece)p).page.getElemByAttr((Piece)p, pieceName, attrName, attrValue);
                else
                    return ((Piece)p).page.getElemByAttr((Piece)p, attrName, attrValue);
            } else
                throw new WebLException(c, callsite, "ArgumentError", toString() + " function expects a page or piece as first argument");
        } catch (TypeCheckException e) {
            throw new Error("internal error");
        }
    }
}
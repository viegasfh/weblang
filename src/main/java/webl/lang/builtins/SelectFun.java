package webl.lang.builtins;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import java.util.*;

public class SelectFun extends AbstractFunExpr
{
    public String toString() {
        return "<Select>";
    }

    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        if (args.size() == 3) {
            Expr e1 = ((Expr)args.elementAt(0)).eval(c);
            Expr e2 = ((Expr)args.elementAt(1)).eval(c);
            Expr e3 = ((Expr)args.elementAt(2)).eval(c);

            if (!(e2 instanceof IntExpr) || !(e3 instanceof IntExpr))
                throw new WebLException(c, callsite, "ArgumentError", "argument does not match");

            int from = (int)((IntExpr)e2).val;
            int to = (int)((IntExpr)e3).val;

            if (e1 instanceof ListExpr) {
                try {
                    return ((ListExpr)e1).getSubList(from, to);
                } catch(IndexOutOfBoundsException i) {
                    throw new WebLException(c, callsite, "IndexRangeError", "index out of list bounds");
                }
            } else if (e1 instanceof StringExpr) {
                String s = ((StringExpr)e1).val();
                try {
                    return Program.Str(s.substring(from, to));
                } catch(StringIndexOutOfBoundsException i) {
                    throw new WebLException(c, callsite, "IndexRangeError", "index out of string bounds");
                }
            } if (e1 instanceof PieceSet) {
                try {
                    return PieceSet.OpSelect((PieceSet)e1, from, to);
                } catch(IndexOutOfBoundsException i) {
                    throw new WebLException(c, callsite, "IndexRangeError", "index out of list bounds");
                }
            }
        } else if (args.size() == 2) {
            Expr e1 = ((Expr)args.elementAt(0)).eval(c);
            Expr e2 = ((Expr)args.elementAt(1)).eval(c);

            if (!(e2 instanceof AbstractFunExpr))
                throw new WebLException(c, callsite, "ArgumentError", this.toString() +
                    " function expects a fun as second argument");
            AbstractFunExpr fn = (AbstractFunExpr)e2;

            Vector arg = new Vector(2);
            arg.addElement(Program.nilval);
            if (e1 instanceof ListExpr) {
                ListExpr R = new ListExpr();
                ListExpr L = (ListExpr)e1;
                Enumeration enumeration = L.getContent();
                while (enumeration.hasMoreElements()) {
                    Expr e = (Expr)enumeration.nextElement();
                    arg.setElementAt(e, 0);
                    Expr res = fn.Apply(c, arg, callsite);
                    if (res instanceof BooleanExpr) {
                        if (res == Program.trueval)
                            R = R.Append(e);
                    } else
                        throw new WebLException(c, callsite, "FunctionReturnTypeNotBoolean",
                            "function argument to Select did not return a boolean value");
                }
                return R;
            } else if (e1 instanceof PieceSet) {
                PieceSet S = (PieceSet)e1;
                PieceSet R = new PieceSet(S.page);
                Enumeration enumeration = S.getContent();
                while (enumeration.hasMoreElements()) {
                    Piece p = (Piece)enumeration.nextElement();
                    arg.setElementAt(p, 0);
                    Expr res = fn.Apply(c, arg, callsite);
                    if (res instanceof BooleanExpr) {
                        if (res == Program.trueval)
                            R.append(p);
                    } else
                        throw new WebLException(c, callsite, "FunctionReturnTypeNotBoolean",
                            "function argument to Select did not return a boolean value");
                }
                return R;
            } else if (e1 instanceof SetExpr) {
                SetExpr S = (SetExpr)e1;
                SetExpr R = new SetExpr();
                Enumeration enumeration = S.getContent();
                while (enumeration.hasMoreElements()) {
                    Expr e = (Expr)enumeration.nextElement();
                    arg.setElementAt(e, 0);
                    Expr res = fn.Apply(c, arg, callsite);
                    if (res instanceof BooleanExpr) {
                        if (res == Program.trueval)
                            R = R.Put(e);
                    } else
                        throw new WebLException(c, callsite, "FunctionReturnTypeNotBoolean",
                            "function argument to Select did not return a boolean value");
                }
                return R;
            } else
                throw new WebLException(c, callsite, "ArgumentError", this.toString() +
                    " expects a list, set, or piece-set as first argument");

        }
        throw new WebLException(c, callsite, "ArgumentError", "argument does not match");
    }
}

package webl.lang;

import java.util.*;
import webl.lang.expr.*;
import webl.lang.builtins.*;

import webl.page.*;

/**
Class <tt>Program</tt> connect the <tt>Parser</tt> class to the internal data structure
of WebL programs (consisting of expressions). It thus separates the front-end responsible for
parsing from the back-end responsible for representation. Nearly all of the methods in this class
call constructors of the <tt>webl.expr</tt> package to create expressions.
*/
public class Program
{

    /** Represents the <tt>true</tt> value in WebL. */
    static final public BooleanExpr trueval = new BooleanExpr(true);

    /** Represents the <tt>false</tt> value in WebL. */
    static final public BooleanExpr falseval = new BooleanExpr(false);

    /** Represents the <tt>nil</tt> value in WebL. */
    static final public NilExpr nilval = new NilExpr();

    /** Represents an error. */
    static final public BooleanExpr errorval = new BooleanExpr(false);

    static public Scope NewUniverseScope(Machine machine) {
        Scope S = new Scope(machine);
        S.define("Native");
        S.define("ARGS");       // webl command line arguments
        S.define("PROPS");       // java properties
        return S;
    }
    
    /** Initializes a context with the default builtin functions. */
    static public Context NewUniverse(Machine M, Module mod, Scope S) throws WebLException {
        Context c = new Context(null, null, mod, S, Str("universe"));
        c.assign("Native", new NativeFun());
        return c;
    }

    static public Expr Int(long val) {
        return new IntExpr(val);
    }

    static public Expr Real(double val) {
        return new RealExpr(val);
    }

    static public StringExpr Str(String name) {
        return StringExpr.NewString(name);
    }

    static public Expr Chr(char ch) {
        return new CharExpr(ch);
    }

    /*
    static public Expr Cons(Expr x, Expr y, int ppos) {
        return new ConsExpr(x, y, ppos);
    }
    */

    static public SetConstructorExpr SetConstructor(int ppos) {
        return new SetConstructorExpr(ppos);
    }

    static public FunConstructorExpr FunConstructor(int ppos) {
        return new FunConstructorExpr(ppos);
    }

    static public MethConstructorExpr MethConstructor(int ppos) {
        return new MethConstructorExpr(ppos);
    }

    static public ObjectConstructorExpr ObjectConstructor(int ppos) {
        return new ObjectConstructorExpr(ppos);
    }

    static public ApplyExpr Apply(Expr x, int ppos) {
        return new ApplyExpr(x, ppos);
    }

    static public Expr Index(Expr x, Expr index, int ppos) {
        return new IndexExpr(x, index, ppos);
    }

    static public Expr Catch(Expr body, VarExpr evar, Vector conds, Vector bodies, int ppos) {
        return new CatchExpr(body, evar, conds, bodies, ppos);
    }

    static public Expr If(Expr cond, Expr trueExpr, Expr falseExpr, int ppos) {
        return new IfExpr(cond, trueExpr, falseExpr, ppos);
    }

    static public Expr While(Expr cond, Expr body, int ppos) {
        return new WhileExpr(cond, body, ppos);
    }

    static public Expr Repeat(Expr body, Expr cond, int ppos) {
        return new RepeatExpr(body, cond, ppos);
    }

    static public Expr Lock(Expr obj, Expr body, int ppos) {
        return new LockExpr(obj, body, ppos);
    }

    static public Expr Every(VarExpr loopvar, Expr collection, Expr body, int ppos) {
        return new EveryExpr(loopvar, collection, body, ppos);
    }

    static public Expr Return(Expr body, int ppos) {
        return new ReturnExpr(body, ppos);
    }
    
    static public Expr Op1(Expr x, Operator op, int ppos) {
        switch (op.op) {
            case Scanner.PLUS: return x;
            case Scanner.MINUS:
                return new NegExpr(x, ppos);
            case Scanner.NOT:
                return new NotExpr(x, ppos);
            default:
                return errorval;
        }
    }

    static public Expr Op2(Scanner S, Expr x, Operator op, Expr y, int ppos) {
         switch (op.op) {
            case Scanner.PLUS:
                return new PlusExpr(x, y, ppos);
            case Scanner.BECOMES:
                if (!(x instanceof IndexExpr || x instanceof VarExpr)) {
                    S.Err("lefthand side of the = operator should be an index expression or variable");
                    return Program.nilval;
                } else
                    return new AssignExpr(x, y, ppos);
            case Scanner.DEF:
                if (!(x instanceof IndexExpr)) {
                    S.Err("lefthand side of the := operator should be an index expression");
                    return Program.nilval;
                } else
                    return new DefExpr((IndexExpr)x, y, ppos);
            case Scanner.MINUS:
                return new MinusExpr(x, y, ppos);
            case Scanner.MUL:
                return new MulExpr(x, y, ppos);
            case Scanner.SLASH:
                return new DivExpr(x, y, ppos);
            case Scanner.DIV:
                return new IntDivExpr(x, y, ppos);
            case Scanner.LT:
                return new LtExpr(x, y, ppos);
            case Scanner.LTE:
                return new LteExpr(x, y, ppos);
            case Scanner.GT:
                return new GtExpr(x, y, ppos);
            case Scanner.GTE:
                return new GteExpr(x, y, ppos);
            case Scanner.EQ:
                return new EqExpr(x, y, ppos);
            case Scanner.NEQ:
                return new NeqExpr(x, y, ppos);
            case Scanner.MOD:
                return new ModExpr(x, y, ppos);
            case Scanner.AND:
                return new AndExpr(x, y, ppos);
            case Scanner.OR:
                return new OrExpr(x, y, ppos);
            case Scanner.QUES:
                return new QuesExpr(x, y, ppos);
            case Scanner.BAR:
                return new BarExpr(x, y, ppos);
            case Scanner.MEMBER:
                return new MemberExpr(x, y, ppos);
               
            case Scanner.INSIDE:
            case Scanner.NOTINSIDE:
            case Scanner.DIRECTLYINSIDE:
            case Scanner.NOTDIRECTLYINSIDE:
            case Scanner.CONTAIN:
            case Scanner.NOTCONTAIN:
            case Scanner.DIRECTLYCONTAIN:
            case Scanner.NOTDIRECTLYCONTAIN:
            case Scanner.AFTER:
            case Scanner.NOTAFTER:
            case Scanner.DIRECTLYAFTER:
            case Scanner.NOTDIRECTLYAFTER:
            case Scanner.BEFORE:
            case Scanner.NOTBEFORE:
            case Scanner.DIRECTLYBEFORE:
            case Scanner.NOTDIRECTLYBEFORE:
            case Scanner.OVERLAP:
            case Scanner.NOTOVERLAP:
            case Scanner.WITHOUT:
            case Scanner.INTERSECT:
                return new PieceSetOpExpr(x, op.op, y, ppos);
            default:
                return errorval;
        }
    }

}


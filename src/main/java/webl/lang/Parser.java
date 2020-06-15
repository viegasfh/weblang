package webl.lang;

import java.lang.*;
import java.io.*;
import java.util.*;
import webl.lang.expr.*;
import webl.util.*;

public class Parser
{
    Machine             machine;
    
    Hashtable           imports = new Hashtable();      // mapping of string to Module
    private Scope       topScope = null;
    private VarExpr     dummyVar = new VarExpr("$", 0, 0);
    
    
    Scanner             S;                              // input stream scanner
    int                 sym;                            // current lookahead token
    int                 pos;                            // offset in input stream

    Logger              log;
    
    public Expr Parse(Machine machine, Scope scope, Scanner S, Logger log) throws IOException {
        this.machine = machine;
        this.S = S;
        this.log = log;
        
        topScope = scope;
        scan();     // read first lookahead token
        
        while (sym == Scanner.IMPORT && getErrorCount() == 0) Imports();
        if (getErrorCount() != 0)   // do not continue if any errors occured
            return null;
            
        Expr result = ParseStatSeq(false);
        if (sym != Scanner.EOF)
            Err("parsing stopped early, perhaps a missing ; ?");
        return result;
    }

    public void Err(String s) {
        S.Err(s);
    }
    
/**
@return Number of errors that occurred during parsing.
*/
    public int getErrorCount() {
        return S.getErrorCount();
    }
    
    // read the next token
    void scan() throws IOException {
        sym = S.scan();
        pos = S.lineOffset;
    }

// scope related stuff

    void OpenScope() {
        topScope = new Scope(topScope);
    }
    
    void CloseScope() {
        topScope = topScope.up;
    }
    
    // define a variable in the current scope
    VarExpr defineVar(String name, boolean export) {
        if (export && !topScope.TopLevel())
            Err("only top level variables may be exported");
            
        VarExpr x = topScope.define(name, export);
        if (x != null) 
            return x;
        else {
            Err("variable " + name + " already defined in this scope");
            return dummyVar;
        }
    }
    
// end scope related stuff

    void Imports() throws IOException {
        scan();         // eat "imports"
        
        while (sym == Scanner.IDENT && getErrorCount() == 0) {
            try {
                if (imports.get(S.name) != null)
                    Err("module " + S.name + " already imported");
                else {
                    Module m = machine.loadModule(S.name, log);
                    if (m != null)
                        imports.put(S.name, m);
                    else
                        Err("Unable to load module " + S.name);
                }
            } catch(FileNotFoundException e) {
                Err("could not locate module " + S.name);
            } catch (IOException e) {
                Err("An IO exception occured while parsing module " + S.name + ", " + e);
            } catch (WebLException e) {
                Err("An exception occured while loading module " + S.name + ": " + e.report());
            }
            scan();
            if (sym == Scanner.COMMA)
                scan();
            else if (sym != Scanner.SEMICOLON)
                Err(", expected");
        }
        if (sym == Scanner.SEMICOLON)
            scan();
        else
            Err("; expected");
    }
    
    VarExpr Designator() throws IOException {
        if (sym == Scanner.IDENT) {
            String s = S.name;
            scan();
            if (sym == Scanner.UNDERSCORE) {            // mod _ var
                scan();
                if (sym == Scanner.IDENT) {
                    String s0 = S.name;
                    scan();
                    Object mod = imports.get(s);
                    if (mod != null) {
                        VarExpr v = ((Module)mod).importVariable(s0);
                        if (v == null) 
                            Err("no variable called " + s0 + " is exported from module " + s);
                        else
                            return v;
                    } else 
                        Err("unknown module " + s);                    
                } else 
                    Err("identifier expected after _");
            } else {
                VarExpr x = topScope.lookup(s);
                if (x != null) 
                    return x;
                else
                    Err("undefined variable " + s);
            }
        } else 
            Err("identifier expected");
            
        return dummyVar;
    }
    
    VarExpr Ident() throws IOException {
        VarExpr x;
        if (sym == Scanner.IDENT) {
            x = topScope.lookup(S.name);
            if (x == null) {
                x = dummyVar;
                Err("undefined variable " + S.name);
            }
            scan();
        } else {
            x = dummyVar;
            Err("identifier expected");     
        }
        return x;
    }
    
    Expr Operand(int ppos) throws IOException {
        Expr x;

        if (sym == Scanner.INT) {
            x = Program.Int(S.ival);
            scan();
        } else if (sym == Scanner.REAL) {
            x = Program.Real(S.rval);
            scan();
        } else if (sym == Scanner.IDENT) {
            x = Designator();
        } else if (sym == Scanner.STRING) {
            x = Program.Str(S.string);
            scan();
        } else if (sym == Scanner.CHAR) {
            x = Program.Chr(S.chr);
            scan();
        } else if (sym == Scanner.TRUE) {
            x = Program.trueval;
            scan();
        } else if (sym == Scanner.FALSE) {
            x = Program.falseval;
            scan();
        } else if (sym == Scanner.NIL) {
            x = Program.nilval;
            scan();
        } else {
            x = Program.nilval;
            Err("operand expected");
            scan();
        }
        return x;
    }

    Expr ElseExpr() throws IOException {
        if (sym == Scanner.ELSE) {
            scan();
            return ParseStatSeq();
        } else if (sym == Scanner.ELSIF) {
            int ppos = pos;
            
            scan();
            Expr condnode = ParseStatSeq();
            if (sym != Scanner.THEN)
                Err("'then' expected");
            else
                scan();
            Expr truenode = ParseStatSeq();
            Expr falsenode = Program.nilval;
            if (sym == Scanner.ELSIF || sym == Scanner.ELSE)
                falsenode = ElseExpr();

            return Program.If(condnode, truenode, falsenode, ppos);
        } else
            return null;
    }

    Expr IfStatement() throws IOException {
        int ppos = pos;
        
        scan();

        Expr condnode = ParseStatSeq();
        if (sym != Scanner.THEN)
            Err("'then' expected");
        else
            scan();

        Expr truenode = ParseStatSeq();
        Expr falsenode = Program.nilval;

        if (sym == Scanner.ELSIF || sym == Scanner.ELSE)
            falsenode = ElseExpr();
        if (sym != Scanner.END)
            Err("'end' expected");
        else
            scan();
       return Program.If(condnode, truenode, falsenode, ppos);
    }

    Expr WhileStatement() throws IOException {
        int ppos = pos;
        
        scan();

        Expr condnode = ParseStatSeq();
        if (sym != Scanner.DO)
            Err("'do' expected");
        else
            scan();

        Expr bodynode = ParseStatSeq();
        if (sym != Scanner.END)
            Err("'end' expected");
        else
            scan();
        return Program.While(condnode, bodynode, ppos);
    }

    Expr RepeatStatement() throws IOException {
        // repeat E until E end
        
        int ppos = pos;
        
        scan();
        
        Expr bodynode = ParseStatSeq();
        if (sym != Scanner.UNTIL)
            Err("'until' expected");
        else
            scan();
            
        Expr condnode = ParseStatSeq();
        if (sym != Scanner.END)
            Err("'end' expected");
        else
            scan();    
        return Program.Repeat(bodynode, condnode, ppos);
    }
    
    Expr FunStatement() throws IOException {
        int ppos = pos;
        
        scan();

        if (sym != Scanner.LPAR) Err("( expected");
        else scan();

        FunConstructorExpr fun = Program.FunConstructor(ppos);
        OpenScope();
        while (sym == Scanner.IDENT) {
            String argname = S.name;
            scan();

            if (fun.addArg(argname)) 
                defineVar(argname, false);
            else
                Err("duplicate formal parameter");
            if (sym == Scanner.COMMA) scan();
            else if (sym != Scanner.RPAR)
                Err(", expected");
        }
        
        if (sym != Scanner.RPAR) Err(") expected");
        else scan();

        fun.setBody(ParseStatSeq());

        fun.setScope(topScope);
        CloseScope();
        
        if (sym != Scanner.END) Err("'end' expected");
        else scan();

        return fun;
    }

    Expr MethStatement() throws IOException {
        int ppos = pos;
        
        scan();

        if (sym != Scanner.LPAR) Err("( expected");
        else scan();

        MethConstructorExpr meth = Program.MethConstructor(ppos);
        
        OpenScope();
        
        if (sym != Scanner.IDENT)
            Err("method must have at least one formal argument");
        while (sym == Scanner.IDENT) {
            String argname = S.name;
            scan();

            if(meth.addArg(argname))
                defineVar(argname, false);
            else
                Err("duplicate formal parameter");
            if (sym == Scanner.COMMA) scan();
            else if (sym != Scanner.RPAR)
                Err(", expected");
        }
        
        if (sym != Scanner.RPAR) Err(") expected");
        else scan();

        meth.setBody(ParseStatSeq());

        meth.setScope(topScope);
        CloseScope();
        
        if (sym != Scanner.END) Err("'end' expected");
        else scan();

        return meth;
    }

    Expr Constant() throws IOException {
        Expr x;

        if (sym == Scanner.INT) {
            x = Program.Int(S.ival);
            scan();
        } else if (sym == Scanner.REAL) {
            x = Program.Real(S.rval);
            scan();
        } else if (sym == Scanner.STRING) {
            x = Program.Str(S.string);
            scan();
        } else if (sym == Scanner.IDENT) {
            x = Program.Str(S.name);
            scan();
        } else if (sym == Scanner.CHAR) {
            x = Program.Chr(S.chr);
            scan();
        } else if (sym == Scanner.TRUE) {
            x = Program.trueval;
            scan();
        } else if (sym == Scanner.FALSE) {
            x = Program.falseval;
            scan();
        } else if (sym == Scanner.NIL) {
            x = Program.nilval;
            scan();
        } else {
            x = null;
            Err("a simple constant was expected for an object fieldname");
            scan();
        }
        return x;
    }
    
    Expr ObjectConstructor() throws IOException {
        ObjectConstructorExpr obj = Program.ObjectConstructor(pos);

        Expr x;

        scan(); // eat opening "[."    
            
        while (sym != Scanner.EOF && sym != Scanner.ROBJ) {
            Expr fieldname = Constant();
            if (fieldname == null)
                break;
            if (sym == Scanner.BECOMES) {
                scan();
                Expr body = ParseExpr(Operator.MAXPREC);
                if(!obj.add(fieldname, body))
                    Err("duplicate object field name " + fieldname);
                
                if (sym == Scanner.COMMA) scan();
                else if (sym != Scanner.ROBJ)
                    Err(", expected");
            } else
                Err("= expected");
        }
        
        if (sym == Scanner.ROBJ) 
            scan();
        else
            Err(".] expected");
            
        return obj;
    }

    // parse ident ( expr, expr, ... )
    Expr Apply(Expr x, int ppos) throws IOException {
        ApplyExpr an = Program.Apply(x, ppos);

        while (sym != Scanner.EOF && sym != Scanner.RPAR) {
            Expr e = ParseExpr(Operator.MAXPREC);
            an.addArg(e);       // !! check for duplicates ?
            if (sym == Scanner.COMMA) scan();
            else if (sym != Scanner.RPAR)
                Err(", expected");
        }
        scan(); // RPAR
        return an;
    }

    Expr Field(Expr x, int ppos) throws IOException {
        if (sym == Scanner.IDENT) {
            StringExpr key = Program.Str(S.name);
            scan();
            return Program.Index(x, key, ppos);
        } else {
            Err("identifier expected");
            return Program.nilval;
        }
    }
    
    Expr Index(Expr x, int ppos) throws IOException {
        Expr i = ParseExpr(Operator.MAXPREC);

        if (sym != Scanner.RBRAK)
            Err("closing ] expected");
        else
            scan();
        return Program.Index(x, i, ppos);
    }
    
    Expr TryStatement() throws IOException {
        int ppos = pos;
        
        scan();
        
        Expr body = ParseStatSeq();
        
        if (sym == Scanner.CATCH) {
            scan();
            
            if (sym == Scanner.IDENT) {
                topScope.OpenBlock();
                VarExpr evar = defineVar(S.name, false);
                
                scan();
                
                Vector conds = new Vector();
                Vector bodies = new Vector();
                
                while (sym == Scanner.ON) {
                    scan();
                    conds.addElement(ParseExpr(Operator.MAXPREC));
                    if (sym == Scanner.DO) {
                        scan();
                        bodies.addElement(ParseStatSeq());
                    } else
                        Err("do expected");
                } 
                topScope.CloseBlock();
                if (sym == Scanner.END) {
                    scan();
                    return Program.Catch(body, evar, conds, bodies, ppos);
                } else
                    Err("end or on expected");
                
            } else
                Err("identifier expected");
        } else
            Err("catch expected");
        return Program.nilval;
    }

    Expr ListStatement() throws IOException {
        ListConstructorExpr l;
        int match;
        
        if (sym == Scanner.LBRAKBAR) {
            l = new ListConstructorExpr(pos, true);
            match = Scanner.BARRBRAK;
        } else {
            l = new ListConstructorExpr(pos, false);
            match = Scanner.RBRAK;
        }
            
        scan();         
        while(sym != Scanner.EOF && sym != match) {
            Expr x = ParseExpr(Operator.MAXPREC);
            l.appendElement(x);
            if(sym == Scanner.COMMA)
                scan();
            else if (sym != match)
                Err(", expected");
        }
        if (sym != match) 
            Err(Scanner.name(match) + " expected");
        else
            scan();
        return l;
    }
    
    Expr SetStatement() throws IOException {
        SetConstructorExpr s = new SetConstructorExpr(pos);
        
        scan();     // get {
        
        while(sym != Scanner.EOF && sym != Scanner.RBRAC) {
            Expr x = ParseExpr(Operator.MAXPREC);
            s.addElement(x);
            if(sym == Scanner.COMMA)
                scan();
            else if (sym != Scanner.RBRAC)
                Err(", expected");
        }
        if (sym != Scanner.RBRAC) 
            Err("} expected");
        else
            scan();
        return s;
    }
    
    Expr EveryStatement() throws IOException {
        // syntax "every" ident "in" E "do" E "end"
        int ppos = pos;
        
        scan();     // get "every"
        if (sym == Scanner.IDENT) {
            String loopvarname = S.name;
            scan();
            if (sym == Scanner.IN) {
                scan();
                Expr collection = ParseExpr(Operator.MAXPREC);
                
                if (sym == Scanner.DO) {
                    scan();
                    
                    topScope.OpenBlock();
                    VarExpr loopvar = defineVar(loopvarname, false);
                    Expr body = ParseStatSeq();
                    topScope.CloseBlock();
                    
                    if (sym == Scanner.END) {
                        scan();
                        return Program.Every(loopvar, collection, body, ppos);
                    } else
                        Err("end expected");
                } else
                    Err("do expected");
            } else
                Err("in expected");
        } else 
            Err("identifier expected");
            
        return Program.nilval;
    }
    
    Expr LockStatement() throws IOException {
        // syntax "lock" E "do" E "end"
        int ppos = pos;
        
        scan(); //get lock
        Expr obj = ParseStatSeq();

        if (sym == Scanner.DO) {
            scan();
            Expr body = ParseStatSeq();
            if (sym == Scanner.END) {
                scan();
                return Program.Lock(obj, body, ppos);
            } else
                Err("end expected");
        } else 
            Err("on expected");
            
        return Program.nilval;
    }
    
    Expr BeginStatement() throws IOException {
        scan();                     // eat "begin"
        Expr R = ParseStatSeq();
        if (sym == Scanner.END)
            scan();
        else
            Err("end expected");
        return R;
    }
    
    Expr ReturnStatement() throws IOException {
        int ppos = pos;
        scan();                     // eat "return"
        
        if (sym == Scanner.SEMICOLON || sym == Scanner.END || sym == Scanner.CATCH || sym == Scanner.UNTIL 
            || sym == Scanner.EOF || sym == Scanner.ELSE || sym == Scanner.ELSIF) {
            return Program.Return(Program.nilval, ppos);
        } else
            return Program.Return(ParseExpr(Operator.MAXPREC), ppos);
    }
    
    Expr SpecialPrefixExpr(int ppos) throws IOException {
        if (sym == Scanner.LOBJ) {          // object
            return ObjectConstructor();
        } else if (sym == Scanner.IF) {     // if statement
            return IfStatement();
        } else if (sym == Scanner.WHILE) {  // while statement
            return WhileStatement();
        } else if (sym == Scanner.FUN) {    // function definition
            return FunStatement();
        } else if (sym == Scanner.METH) {   // method definition
            return MethStatement();
        } else if (sym == Scanner.TRY) {    // try catch statement
            return TryStatement();
        } else if (sym == Scanner.LBRAK || sym == Scanner.LBRAKBAR) {
            return ListStatement();
        } else if (sym == Scanner.LBRAC) {
            return SetStatement();
        } else if (sym == Scanner.EVERY) {
            return EveryStatement();
        } else if (sym == Scanner.LOCK) {
            return LockStatement();
        } else if (sym == Scanner.REPEAT) {
            return RepeatStatement();
        } else if (sym == Scanner.BEGIN) {
            return BeginStatement();
        } else if (sym == Scanner.RETURN) {
            return ReturnStatement();
        } else if (sym == Scanner.LPAR) {
            scan();
            
            Expr x = ParseExpr(Operator.MAXPREC);
            if (sym == Scanner.RPAR)
                scan();
            else
                Err(") expected");
            return x;
        } else {
            Err("Internal error: unknown special prefix expression");
            return Program.nilval;
        }
    }

    Expr SpecialInfixExpr(Expr x, Operator op, int ppos) throws IOException {
        if (op.op == Scanner.LPAR)
            return Apply(x, ppos);
        else if (op.op == Scanner.PERIOD)
            return Field(x, ppos);
        else if (op.op == Scanner.LBRAK)
            return Index(x, ppos);
        else {
            Err("Internal error: unknown special prefix expression");
            return Program.nilval;
        }
    }

    Expr ApplyPrefix(Expr x, Operator op, int ppos) throws IOException {
        if (op.fix == Operator.PREFIX) {
            scan();
            x = Program.Op1(ParseExpr(op.rprec), op, ppos);
            if (x == Program.errorval)
                Err("unknown monadic operator");
        } else if (op.fix == Operator.BRACKETFIX) {
            scan();
            x = Program.Op1(ParseExpr(Operator.MAXPREC), op, ppos);
            if (x == Program.errorval)
                Err("unknown monadic operator");
            if (sym == op.match) 
                scan();
            else
                Err("closing " + Scanner.name(op.match) + " expected");
        } else if (op.fix == Operator.LEFTBRACKETFIX) {
            scan();
            x = ParseExpr(op.rprec);
            if (sym == op.match) {
                scan();
                x = Program.Op2(S, x, op, ParseExpr(op.rprec), ppos);
                if (x == Program.errorval)
                    Err("unknown dyadic operator");
            } else
                Err("closing " + Scanner.name(op.match) + " expected");
        } else if (op.fix == Operator.SPREFIX) {
            x = SpecialPrefixExpr(ppos);
        } else
            Err("Internal error: unknown prefix class operator");

        return x;
    }

    Expr ApplyInfix(Expr x, Operator op, int ppos) throws IOException {
        if (op.fix == Operator.INFIX) {
            x = Program.Op2(S, x, op, ParseExpr(op.rprec), ppos);
            if (x == Program.errorval)
                Err("unknown dyadic operator");
        } else if (op.fix == Operator.POSTFIX) {
            x = Program.Op1(x, op, ppos);
            if (x == Program.errorval)
                Err("unknown dyadic operator");
        } else if (op.fix == Operator.RIGHTBRACKETFIX) {
            x = Program.Op2(S, x, op, ParseExpr(Operator.MAXPREC), ppos);
            if (x == Program.errorval)
                Err("unknown dyadic operator");
            if (sym == op.match)
                scan();
            else
                Err("closing " + Scanner.name(op.match) + " expected");
        } else if (op.fix == Operator.SINFIX) {
            x = SpecialInfixExpr(x, op, ppos);
        } else
            Err("Internal error: unknown infix class operator " + op.fix);

        return x;
    }

    Expr ParseExpr(int preclevel) throws IOException {
        Operator op;
        Expr x = null;
        int ppos;

        // handle the prefix class operators
        op = Operator.PrefixClass(sym); ppos = pos;
        if (op != null) {
            x = ApplyPrefix(x, op, ppos);
        } else
            x = Operand(ppos);

        // handle the infix class operators
        op = Operator.InfixClass(sym); ppos = pos;
        while (op != null && op.lprec < preclevel) {
            scan();
            x = ApplyInfix(x, op, ppos);
            op = Operator.InfixClass(sym); ppos = pos;
        }
        return x;
    }
    
    void ParseVar0(SequenceExpr SS, boolean export) throws IOException {
        if (sym == Scanner.IDENT) {
            String name = S.name;
            Expr V = defineVar(name, export);
            scan();
            
            if (sym == Scanner.DEF) {
                Err("= expected");
                sym = Scanner.BECOMES;
            }
            if (sym == Scanner.BECOMES) {
                int ppos = pos;
                scan();
                Expr value = ParseExpr(Operator.MAXPREC);
                SS.append(Program.Op2(S, V, Operator.InfixClass(Scanner.BECOMES), value, ppos));
            }
        } else
            Err("identifier expected");
    }
    
    void ParseVar(SequenceExpr SS) throws IOException {        // note: can return null !
        // syntax ["export"] "var" ident [ "=" E ] { "," ident [ "=" E ] }
        
        boolean export = false;
        if (sym == Scanner.EXPORT) {
            if (topScope.TopLevel())
                export = true;
            else
                Err("only top level variables may be exported");
            scan();
        }
        
        if (sym == Scanner.VAR)
            scan(); // get var
        else
            Err("var expected");
            
        ParseVar0(SS, export);
        while (sym == Scanner.COMMA) {
            int ppos = pos;
            scan();
            ParseVar0(SS, export);
        }
    }
        
    void ParseVarOrE(SequenceExpr SS) throws IOException {
        if (sym == Scanner.VAR || sym == Scanner.EXPORT)
            ParseVar(SS);
        else
            SS.append(ParseExpr(Operator.MAXPREC));
    }
    
    Expr ParseStatSeq() throws IOException {
        return ParseStatSeq(true);
    }
    
    Expr ParseStatSeq(boolean opennewblock) throws IOException {
        SequenceExpr SS = new SequenceExpr(pos);
        
        if (opennewblock)
            topScope.OpenBlock();
        
        ParseVarOrE(SS);
        while(sym == Scanner.SEMICOLON) {
            int ppos = pos;
            scan();
            
            if (sym == Scanner.END || sym == Scanner.CATCH || sym == Scanner.UNTIL 
                || sym == Scanner.EOF || sym == Scanner.ELSE || sym == Scanner.ELSIF) {
                // SS.append(Program.nilval);  do not append nil value
                break;
            }
                
            ParseVarOrE(SS);
        }
        if (opennewblock)
            topScope.CloseBlock();
            
        return SS;
    }
}


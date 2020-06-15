package webl.lang;

/** Encapsulates WebL operators, their precedence and associativity. */
public class Operator
{
    public int op;              // corresponds to the scanner token classes
    public int match;           // matching closing token for bracketfix
    public int lprec, rprec;    // left and right precedence levels (smaller numbers bind tighter)
    public int assoc;           // associativity
    public int fix;             // prefix, infix, postfix, bracketfix, rightbracketfix
    
    public final static int MAXPREC = 1000;     // must be > maximum precedence level
    
    public final static int LEFT_ASSOC = 0, RIGHT_ASSOC = 1, NON_ASSOC = 2;
    
    // normal fix codes
    public final static int PREFIX = 0, INFIX = 1, POSTFIX = 2;
    public final static int BRACKETFIX = 3, RIGHTBRACKETFIX = 4, LEFTBRACKETFIX = 5;
    
    // codes for exceptional handling by the parser
    public final static int SPREFIX = 6, SINFIX = 7;

    public Operator (int op, int prec, int fix, int assoc) {
        this.op = op;
        this.lprec = prec;
        this.rprec = prec;
        if (assoc == LEFT_ASSOC) this.rprec--;
        else if (assoc == RIGHT_ASSOC) this.lprec--;
        this.assoc = assoc;
        this.fix = fix;
    }
    
    public Operator (int op, int match, int prec, int fix, int assoc) {
        this.op = op;
        this.match = match;
        this.lprec = prec;
        this.rprec = prec;
        if (assoc == LEFT_ASSOC) this.rprec--;
        else if (assoc == RIGHT_ASSOC) this.lprec--;
        this.assoc = assoc;
        this.fix = fix;
    }
    
    public static final Operator[] ops =
        {
         new Operator(Scanner.IF, 5, SPREFIX, NON_ASSOC),
         new Operator(Scanner.LPAR, 5, SPREFIX, NON_ASSOC),
         new Operator(Scanner.LBRAK, 5, SPREFIX, NON_ASSOC),
         new Operator(Scanner.LBRAKBAR, 5, SPREFIX, NON_ASSOC),
         new Operator(Scanner.LBRAC, 5, SPREFIX, NON_ASSOC),
         new Operator(Scanner.LOBJ, 5, SPREFIX, NON_ASSOC),
         new Operator(Scanner.WHILE, 5, SPREFIX, NON_ASSOC),
         new Operator(Scanner.FUN, 5, SPREFIX, NON_ASSOC),
         new Operator(Scanner.METH, 5, SPREFIX, NON_ASSOC),
         new Operator(Scanner.TRY, 5, SPREFIX, NON_ASSOC),
         new Operator(Scanner.EVERY, 5, SPREFIX, NON_ASSOC),
         new Operator(Scanner.LOCK, 5, SPREFIX, NON_ASSOC),
         new Operator(Scanner.REPEAT, 5, SPREFIX, NON_ASSOC),
         new Operator(Scanner.BEGIN, 5, SPREFIX, NON_ASSOC),
         new Operator(Scanner.RETURN, 5, SPREFIX, NON_ASSOC),
         
         new Operator(Scanner.LBRAK, 10, SINFIX, LEFT_ASSOC),
         new Operator(Scanner.PERIOD, 10, SINFIX, LEFT_ASSOC),
         new Operator(Scanner.LPAR, 10, SINFIX, LEFT_ASSOC),
         
         new Operator(Scanner.PLUS, 20, PREFIX, RIGHT_ASSOC),
         new Operator(Scanner.MINUS, 20, PREFIX, RIGHT_ASSOC),
         new Operator(Scanner.NOT, 20, PREFIX, RIGHT_ASSOC),
         
         new Operator(Scanner.MUL, 30, INFIX, LEFT_ASSOC),
         new Operator(Scanner.SLASH, 30, INFIX, LEFT_ASSOC),
         new Operator(Scanner.DIV, 30, INFIX, LEFT_ASSOC),
         new Operator(Scanner.MOD, 30, INFIX, LEFT_ASSOC),
         
         new Operator(Scanner.PLUS, 40, INFIX, LEFT_ASSOC),
         new Operator(Scanner.MINUS, 40, INFIX, LEFT_ASSOC),
         
         new Operator(Scanner.MEMBER, 45, INFIX, LEFT_ASSOC),
         
         new Operator(Scanner.INSIDE, 45, INFIX, LEFT_ASSOC),
         new Operator(Scanner.NOTINSIDE, 45, INFIX, LEFT_ASSOC),
         new Operator(Scanner.DIRECTLYINSIDE, 45, INFIX, LEFT_ASSOC),
         new Operator(Scanner.NOTDIRECTLYINSIDE, 45, INFIX, LEFT_ASSOC),
         new Operator(Scanner.CONTAIN, 45, INFIX, LEFT_ASSOC),
         new Operator(Scanner.NOTCONTAIN, 45, INFIX, LEFT_ASSOC),
         new Operator(Scanner.DIRECTLYCONTAIN, 45, INFIX, LEFT_ASSOC),
         new Operator(Scanner.NOTDIRECTLYCONTAIN, 45, INFIX, LEFT_ASSOC),
         new Operator(Scanner.AFTER, 45, INFIX, LEFT_ASSOC),
         new Operator(Scanner.NOTAFTER, 45, INFIX, LEFT_ASSOC),
         new Operator(Scanner.DIRECTLYAFTER, 45, INFIX, LEFT_ASSOC),
         new Operator(Scanner.NOTDIRECTLYAFTER, 45, INFIX, LEFT_ASSOC),
         new Operator(Scanner.BEFORE, 45, INFIX, LEFT_ASSOC),
         new Operator(Scanner.NOTBEFORE, 45, INFIX, LEFT_ASSOC),
         new Operator(Scanner.DIRECTLYBEFORE, 45, INFIX, LEFT_ASSOC),
         new Operator(Scanner.NOTDIRECTLYBEFORE, 45, INFIX, LEFT_ASSOC),
         new Operator(Scanner.OVERLAP, 45, INFIX, LEFT_ASSOC),
         new Operator(Scanner.NOTOVERLAP, 45, INFIX, LEFT_ASSOC),
         new Operator(Scanner.WITHOUT, 45, INFIX, LEFT_ASSOC),
         new Operator(Scanner.INTERSECT, 45, INFIX, LEFT_ASSOC),
         
         new Operator(Scanner.LT, 60, INFIX, LEFT_ASSOC),
         new Operator(Scanner.LTE, 60, INFIX, LEFT_ASSOC),
         new Operator(Scanner.GT, 60, INFIX, LEFT_ASSOC),
         new Operator(Scanner.GTE, 60, INFIX, LEFT_ASSOC),
         
         new Operator(Scanner.EQ, 70, INFIX, LEFT_ASSOC),
         new Operator(Scanner.NEQ, 70, INFIX, LEFT_ASSOC),
         
         new Operator(Scanner.AND, 80, INFIX, RIGHT_ASSOC),
         
         new Operator(Scanner.OR, 90, INFIX, RIGHT_ASSOC),
         
         new Operator(Scanner.BECOMES, 100, INFIX, RIGHT_ASSOC),
         new Operator(Scanner.DEF, 100, INFIX, RIGHT_ASSOC),
         
         new Operator(Scanner.BAR, 110, INFIX, RIGHT_ASSOC),
         new Operator(Scanner.QUES, 110, INFIX, RIGHT_ASSOC),
        };
        
    public static Operator InfixClass(int sym) {
        int i = 0;
        while (i < ops.length) {
            if (ops[i].op == sym &&
                (ops[i].fix == INFIX || ops[i].fix == RIGHTBRACKETFIX || ops[i].fix == POSTFIX ||
                ops[i].fix == SINFIX))
                return ops[i];
             i++;
        }
        return null;
    }

    public static Operator PrefixClass(int sym) {
        int i = 0;
        while (i < ops.length) {
            if (ops[i].op == sym &&
            (ops[i].fix == PREFIX || ops[i].fix == BRACKETFIX || ops[i].fix == LEFTBRACKETFIX ||
            ops[i].fix == SPREFIX ))
                return ops[i];
            i++;
        }
        return null;
    }
        
}

/* Operator table

5   ()                          normal grouping
    []                          list spec
    {}                          set spec
    [. .]                       object spec
    allcontrolstatments
    var                 R       unary
    
10  []                  L       array access
    .                   L       field access
    ()                  L       fun/meth call

20  + - n!              R       unary prefix

30  * / mod             L       binary

40  + -                 L       binary

45  member              L
    inside              L
    !inside             L
    directlyinside      L
    !directlyinside     L
    contain             L
    !contain            L
    directlycontain     L
    !directlycontain    L
    after               L
    !after              L
    directlyafter       L
    !directlyafter      L
    before              L
    !before             L
    directlybefore      L
    !directlybefore     L
    overlap             L
    !overlap            L
    without             L
    intersect           L

60  < <=                L
    > >=                L

70  == !=               L

80  and                 R

90  or                  R

100 := =                R

110 | || ?              R


*/
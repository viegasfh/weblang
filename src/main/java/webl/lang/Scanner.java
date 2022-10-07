package webl.lang;

// scanner for WebL syntax

import java.lang.*;
import java.io.*;
import webl.util.Logger;

/**
Scanner class for scanning WebL program tokens and operators. The exported
constants indicate what token has been read.
*/
public final class Scanner
{
    String filename;        // file name being read from
    Reader str;             // input stream
    int ch;                 // lookahead character
    int pushedsym = 0;
    
    // counters:
    int lineno;             // line offset in file
    int noerrors = 0;       // no of errors occuring during scanning
    
    int lasterror = -1;
    
    char EOL = '\n';        // end-of-line character
    
    // scanner constants:
    public final static int ILLEGAL = 0;
    public final static int PLUS = 1, MINUS = 2, MUL = 3, DIV = 4;
    public final static int LPAR = 5, RPAR = 6, LBRAK = 7, RBRAK = 8;
    public final static int INT = 9, IDENT = 10, STRING = 11;
    public final static int EQ = 12, IF = 13, THEN = 14, ELSE = 15, END = 16;
    public final static int COMMA = 17, COLON = 18, BECOMES = 19, PERIOD = 20, SEMICOLON = 21;
    public final static int LBRAC = 22, RBRAC = 23, ELSIF = 24, TRUE = 25, FALSE = 26;
    public final static int NOT = 27, MOD = 28, GT = 29, GTE = 30, LT = 31, LTE = 32;
    public final static int NEQ = 33, AND = 34, OR = 35, BAR = 36, QUES = 37, DEF = 38;
    public final static int NIL = 39, WHILE = 40, DO = 41, FUN = 42, METH = 43, CHAR = 44, REAL = 45;
    public final static int TRY = 46, CATCH = 47, SLASH = 48, UNDERSCORE = 49;
    public final static int LOBJ = 50, ROBJ = 51, ON = 52, IN = 53, EVERY = 54, LOCK = 55;
    public final static int VAR = 57, PARALLEL = 58;
    public final static int OVERLAP = 70, NOTOVERLAP = 71; 
    public final static int NOTINSIDE = 72, DIRECTLYINSIDE = 73, NOTDIRECTLYINSIDE = 74; 
    public final static int CONTAIN = 75, NOTCONTAIN = 76, DIRECTLYCONTAIN = 77, NOTDIRECTLYCONTAIN = 78;
    public final static int BEFORE = 79, NOTBEFORE = 80, DIRECTLYBEFORE = 81, NOTDIRECTLYBEFORE = 82;
    public final static int AFTER = 83, NOTAFTER = 84, DIRECTLYAFTER = 85, NOTDIRECTLYAFTER = 86;
    public final static int WITHOUT = 87, INTERSECT = 88, REPEAT = 89, UNTIL = 90, IMPORT = 91, INSIDE = 92;
    public final static int BEGIN = 93, MEMBER = 94, EXPORT = 95, LBRAKBAR = 96, BARRBRAK = 97, RETURN = 98;
    
    public final static int EOF = -1;
    
/** Line number in file where scanned token starts. */    
    public int lineOffset;
    
/** The identifier scanned when <tt>scan()== IDENT</tt>. */
    public String name;    
    
/** The "string" scanned when <tt>scan() == STRING</tt>. */
    public String string;   // token STRING
    
/** The real scanned when <tt>scan() == REAL</tt>.*/
    public double rval;     // token REAL
    
/** The integer scanned when <tt>scan() == INT</tt>.*/
    public long ival;       // token INT
    
/** The character scanned when <tt>scan() == CHAR</tt>.*/
    public char chr;        // token CHAR
   
    private Logger log;        // place where errors are written to
    
/**
@param input Input stream from which to scan.
@param output Scanning errors are reported to this stream.
*/
    public Scanner(String filename, Reader input, Logger log, int startlineno) throws IOException {
        this.filename = filename;
        this.log = log;
        str = new LineNumberReader(input);
        lineno = startlineno;
        get();
        
        if (ch == '#') {  // skip first line of the script
            while (ch != EOF && ch != EOL)
                get();
        }
    }
   
/** Given a token code (IDENT, INT, CHAR, etc.) return the corresponding WebL token. */   
    public static String name(int op) {
        switch (op) {
            case ILLEGAL: return ("ILLEGAL");
            case PLUS: return "+";
            case MINUS: return "-";
            case MUL: return "*";
            case DIV: return "div";
            case LPAR: return "(";
            case RPAR: return ")";
            case LBRAK: return "[";
            case RBRAK: return "]";
            case INT: return "INTEGER";
            case IDENT: return "IDENT";
            case STRING: return "STRING";
            case EQ: return "==";
            case IF: return "if";
            case THEN: return "then";
            case ELSE: return "else";
            case END: return "end";
            case COMMA: return ",";
            case COLON: return ":";
            case BECOMES: return "=";
            case PERIOD: return ".";
            case SEMICOLON: return ";";
            case LBRAC: return "{";
            case RBRAC: return "}";
            case TRUE: return "true";
            case FALSE: return "false";
            case NOT: return "!";
            case MOD: return "mod";
            case GT: return ">";
            case GTE: return ">=";
            case LT: return "<";
            case LTE: return "<=";
            case NEQ: return "!=";
            case AND: return "and";
            case OR: return "or";
            case BAR: return "|";
            case QUES: return "?";
            case DEF: return ":=";
            case NIL: return "nil";
            case WHILE: return "while";
            case DO: return "do";
            case FUN: return "fun";
            case METH: return "meth";
            case CHAR: return "CHAR";
            case REAL: return "REAL";
            case TRY: return "try";
            case CATCH: return "catch";
            case SLASH: return "/";
            case UNDERSCORE: return "_";
            case LOBJ: return "[.";
            case ROBJ: return ".]";
            case ON: return "on";
            case IN: return "in";
            case EVERY: return "every";
            case LOCK: return "lock";
            case VAR: return "var";
            case PARALLEL: return "parallel";
            case OVERLAP: return "overlap";
            case NOTOVERLAP: return "!overlap";
            case INSIDE: return "inside";
            case NOTINSIDE: return "!inside";
            case DIRECTLYINSIDE: return "directlyinside";
            case NOTDIRECTLYINSIDE: return "!directlyinside";
            case CONTAIN: return "contain";
            case NOTCONTAIN: return "!contain";
            case DIRECTLYCONTAIN: return "directlycontain";
            case NOTDIRECTLYCONTAIN: return "!directlycontain";
            case BEFORE: return "before";
            case NOTBEFORE: return "!before";
            case DIRECTLYBEFORE: return "directlybefore";
            case NOTDIRECTLYBEFORE: return "!directlybefore";
            case AFTER: return "after";
            case NOTAFTER: return "!after";
            case DIRECTLYAFTER: return "directlyafter";
            case NOTDIRECTLYAFTER: return "!directlyafter";
            case WITHOUT: return "without";
            case INTERSECT: return "intersect";
            case REPEAT: return "repeat";
            case UNTIL: return "until";
            case IMPORT: return "import";
            case BEGIN: return "begin";
            case MEMBER: return "member";
            case EXPORT: return "export";
            case LBRAKBAR: return "[|";
            case BARRBRAK: return "|]";
            case RETURN: return "return";
            default:
                return "UNKNOWN";
        }
    }
    
/** Report an error to the error stream initialized by the class constructor. */    
    public void Err(String err) {
        noerrors++;
        if (lineno > lasterror) {
            log.println(err + " (" + filename + ", " + lineno +")");
            lasterror = lineno;
        }
    }

/**
@return Number of times <tt>Err</tt> was invoked.
*/
    public int getErrorCount() {
        return noerrors;
    }
    
    
    // read the next input character, advancing counters
    void get() throws IOException {
        ch = str.read();
        if (ch == EOL) lineno++;
    }
    
    // scan a number
    int Num() throws IOException {
        boolean isreal = false;
        StringBuffer s = new StringBuffer();
        
        while (ch >= '0' && ch <= '9') {
            s.append((char)ch); get();
        }
        if (ch == '.') {        // has a factional part
            isreal = true;
            s.append((char)ch);
            get();
            
            while (ch >= '0' && ch <= '9') {
                s.append((char)ch); get();
            }
        }
        if (ch == 'e' || ch == 'E') {   // has an exponent
            isreal = true;
            s.append((char)ch);
            get();
            if (ch == '+' || ch == '-') {   // optional + or -
                s.append((char)ch);
                get();
            }
            while (ch >= '0' && ch <= '9') {
                s.append((char)ch); get();
            }
        }
        
        if (isreal) {
            try {
              rval = Double.valueOf(s.toString()).doubleValue();
            } catch (NumberFormatException e) {
                Err("not a real number");
                rval = 1;
            }
            return REAL;
        } else {    // an integer
            try {
              ival = Long.valueOf(s.toString()).longValue();
            } catch (NumberFormatException e) {
                Err("not an integer number");
                ival = 1;
            }
            return INT;
        }
    }
    
    
    // scan an identifier
    void Ident() throws IOException {
        StringBuffer s = new StringBuffer();
        
        while (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9') {
            s.append((char)ch);
            get();
        }
        name = s.toString();
    }
    
    boolean HexDigit(int ch) {
        return ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'f' || ch >= 'A' && ch <= 'F';
    }
    
    int HexVal(int ch) {
        if (ch >= '0' && ch <= '9')
            return ch - '0';
        else if (ch >= 'A' && ch <= 'F')
            return ch - 'A' + 10;
        else
            return ch - 'a' + 10;
    }
    
    int getEscape() throws IOException {
        get();      // eat the \ character
        
        switch(ch) {
            case 'b':
                get(); return '\b';
            case 't':
                get(); return '\t';
            case 'n':
                get(); return '\n';
            case 'f':
                get(); return '\f';
            case 'r':
                get(); return '\r';
            case '"':
                get(); return '"';
            case '\'':
                get(); return '\'';
            case '\\':
                get(); return '\\';
            case 'u':   // unicode escape
                get();
                if (HexDigit(ch)) {
                    int val = HexVal(ch);
                    
                    get();
                    while (HexDigit(ch)) {
                        val = val * 16 + HexVal(ch);
                        get();
                    }
                    return (char)val;
                }
                Err("unicode character does not contain a hex number");
                break;
            default:
                if (ch >= '0' && ch <= '7') {   // octal 
                    int val = ch - '0';
                    
                    get();
                    while (ch >= '0' && ch <= '7') {
                        val = val * 8 + ch - '0';
                        get();
                    }
                    return (char)val;
                }
                Err("illegal character escape");
        }
        return EOF;
    }
    
    int Char() throws IOException {
        int delim = ch;
        
        get();
        if (ch == '\\') {
            chr = (char)getEscape();
        } else {
            chr = (char)ch;
            get();
        }
        
        if (ch == delim)
            get();
        else
            Err("unterminated character string");
        return CHAR;
    }
    
    int Str() throws IOException {
        StringBuffer s = new StringBuffer();
        int delim = ch;
        
        get();
        while (ch != delim && ch != EOF) {
            if (ch == '\\') {
                s.append((char)getEscape());
            } else {
                s.append((char)ch);
                get();
            }
        }
        string = s.toString();
        if (ch == delim)
            get();
        else
            Err("unterminated string");
        return STRING;
    }
    
    int ThisHere() throws IOException {
        StringBuffer s = new StringBuffer();
        int delim = ch;
        
        get();
        while (ch != delim && ch != EOF) {
            s.append((char)ch);
            get();
        }
        string = s.toString();
        if (ch == delim)
            get();
        else
            Err("unterminated string");
        return STRING;
    }
    
    void Comment() throws IOException {
        int nesting = 1;
        
        for (;;) {
            if (ch == EOF) return;
            else if (ch == '/') {
                get();
                if (ch == '*') {
                    get();
                    nesting++;
                }
            } else if (ch == '*') {
                get();
                if (ch == '/') {
                    get();
                    if (--nesting == 0) return;
                }
            } else
                get();
        }
    }

    int IdentCheck(String name) {
        switch(name.charAt(0)) {
            case 'a':
                if (name.equals("and")) return AND;
                else if (name.equals("after")) return AFTER;
                return IDENT;
            case 'b':
                if (name.equals("before")) return BEFORE;
                else if (name.equals("begin")) return BEGIN;
                return IDENT;
            case 'c':
                if (name.equals("catch")) return CATCH;
                else if (name.equals("contain")) return CONTAIN;
                return IDENT;
            case 'd':
                if (name.equals("do")) return DO;
                else if (name.equals("div")) return DIV;
                else if (name.equals("directlyafter")) return DIRECTLYAFTER;
                else if (name.equals("directlybefore")) return DIRECTLYBEFORE;
                else if (name.equals("directlycontain")) return DIRECTLYCONTAIN;
                else if (name.equals("directlyinside")) return DIRECTLYINSIDE;
                return IDENT;
            case 'e':
                if (name.equals("else")) return ELSE;
                else if (name.equals("elsif")) return ELSIF;
                else if (name.equals("end")) return END;
                else if (name.equals("every")) return EVERY;
                else if (name.equals("export")) return EXPORT;
                return IDENT;
            case 'f':
                if (name.equals("false")) return FALSE;
                else if (name.equals("fun")) return FUN;
                return IDENT;
            case 'i':
                if (name.equals("if")) return IF;
                else if (name.equals("in")) return IN;
                else if (name.equals("inside")) return INSIDE;
                else if (name.equals("intersect")) return INTERSECT;
                else if (name.equals("import")) return IMPORT;
                return IDENT;
            case 'l':
                if (name.equals("lock")) return LOCK;
                return IDENT;
            case 'm':
                if (name.equals("mod")) return MOD;
                else if (name.equals("meth")) return METH;
                else if (name.equals("member")) return MEMBER;
                return IDENT;
            case 'n':
                if (name.equals("nil")) return NIL;
                return IDENT;
            case 'o':
                if (name.equals("or")) return OR;
                else if (name.equals("on")) return ON;
                else if (name.equals("overlap")) return OVERLAP;
                return IDENT;
            case 'p':
                if (name.equals("parallel")) return PARALLEL;
                return IDENT;
            case 'r':
                if (name.equals("repeat")) return REPEAT;
                else if (name.equals("return")) return RETURN;
                return IDENT;
            case 't':
                if (name.equals("then")) return THEN;
                else if (name.equals("true")) return TRUE;
                else if (name.equals("try")) return TRY;
                return IDENT;
            case 'u':
                if (name.equals("until")) return UNTIL;
                return IDENT;
            case 'w':
                if (name.equals("while")) return WHILE;
                else if (name.equals("without")) return WITHOUT;
                return IDENT;
            case 'v':
                if (name.equals("var")) return VAR;
                return IDENT;
            default:
                return IDENT;
        }
    }
    
/**
@return The next symbol in the program. <tt>EOF</TT> indicates that the end of the input stream
reached.
*/
    public int scan() throws IOException {
    
        if (pushedsym != 0) {
            int r = pushedsym;
            pushedsym = 0;
            return r;
        }
        
        // scan over whitespace and comments
        exit:
        while (true) { 
            switch (ch) {
                case EOF: return EOF;
                case ' ':
                case '\n':
                case '\r':
                case '\t': get();
                    break;
                case '/':
                    get();
                    if (ch == '/') {    // comment, skip to the end of the line
                        get();
                        while (ch != EOF && ch != EOL) get();
                        break;
                    } else if (ch == '*') {
                        get();
                        Comment();
                        break;
                    } else
                        return SLASH;
                default:
                    break exit;
            };
        }
        
        lineOffset = lineno;  // scanned token starts here
        
        // scan tokens
        switch (ch) {
            case 0: return EOF;
            case '+': get(); return PLUS;
            case '-': get(); return MINUS;
            case '*': get(); return MUL;
            case '(': get(); return LPAR;
            case ')': get(); return RPAR;
            case '[': 
                get(); 
                if (ch == '.') { get(); return LOBJ; }
                else if (ch == '|') { get(); return LBRAKBAR; }
                return LBRAK;
            case ']': get(); return RBRAK;
            case '{': get(); return LBRAC;
            case '}': get(); return RBRAC;
            case '=':
                get();
                if (ch == '=') { get(); return EQ; }
                return BECOMES;
            case ',': get(); return COMMA;
            case ';': get(); return SEMICOLON;
            case '.':
                get(); 
                if (ch == ']') { get(); return ROBJ; }
                return PERIOD;
            case '|': 
                get(); 
                if (ch == ']') { get(); return BARRBRAK; }
                return BAR;
            case '?': get(); return QUES;
            case '>':
                get();
                if (ch == '=') { get(); return GTE; }
                return GT;
            case '<':
                get();
                if (ch == '=') { get(); return LTE; }
                return LT;
            case ':': 
                get();
                if (ch == '=') { get(); return DEF; }
                return COLON;
            case '!':
                get();
                if (ch == '=') { get(); return NEQ; }
                else if ((ch >= 'a' && ch <= 'd') || ch == 'i' || ch == 'o') {
                    Ident();
                    if (name.equals("after")) return NOTAFTER;
                    else if (name.equals("before")) return NOTBEFORE;
                    else if (name.equals("contain")) return NOTCONTAIN;
                    else if (name.equals("inside")) return NOTINSIDE;
                    else if (name.equals("directlyafter")) return NOTDIRECTLYAFTER;
                    else if (name.equals("directlybefore")) return NOTDIRECTLYBEFORE;
                    else if (name.equals("directlycontain")) return NOTDIRECTLYCONTAIN;
                    else if (name.equals("directlyinside")) return NOTDIRECTLYINSIDE;
                    else if (name.equals("overlap")) return NOTOVERLAP;
                    else {
                        pushedsym = IdentCheck(name);
                        return NOT;
                    }
                }
                return NOT;
            case '\'':
                return Char();
            case '\"':
                return Str();
            case '`':
                return ThisHere();
            case '_':
                get();
                return UNDERSCORE;
            
            default:
                if (ch >= 'a' && ch <= 'z') {
                    Ident();
                    return IdentCheck(name);
                } else if (ch >= '0' && ch <= '9')
                    return Num();
                else if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z') {
                    Ident();
                    return IDENT;
                } else {
                    Err("illegal symbol " + (char)ch);
                    get();
                    return ILLEGAL; // not a legal character
                }
        }
    }

}


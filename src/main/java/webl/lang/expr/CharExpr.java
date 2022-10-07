package webl.lang.expr;

public class CharExpr extends ValueExpr
{
    public char ch;

    public CharExpr(char ch) {
        super(-1);
        this.ch = ch;
    }

    public String getTypeName() {
        return "char";
    }
    
    public String toString() {
        return "'" + ch + "'";
    }
    
    public String print() {
        return String.valueOf(ch);
    }
    
    public int hashCode() {
        return (int)ch;
    }
    
    public boolean equals(Object obj) {
        return (obj instanceof CharExpr) && ((CharExpr)obj).ch == ch;
    }
}
package webl.lang.expr;

public class BooleanExpr extends ValueExpr
{
    public boolean val;

    public BooleanExpr(boolean b) {
        super(-1);
        val = b;
    }

    public String getTypeName() {
        return "bool";
    }
    
    public String toString() {
        return val ? "true" : "false";
    }
    
    public int hashCode() {
        return val ? 1231 : 1237;
    }
    
    public boolean equals(Object obj) {
        return (obj instanceof BooleanExpr) && ((BooleanExpr)obj).val == val;
    }

}
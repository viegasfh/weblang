package webl.lang.expr;
import webl.lang.*;

public class IntExpr extends ValueExpr
{
    public long val;

    public IntExpr(long val) {
        super(-1);
        this.val = val;
    }

    public String getTypeName() {
        return "int";
    }
    
    public String toString() {
        return Long.toString(val);
    }
    
    public int hashCode() {
        return (int)val;
    }
    
    public boolean equals(Object obj) {
        return (obj instanceof IntExpr) && ((IntExpr)obj).val == val;
    }
    
}
package webl.lang.expr;

public class RealExpr extends ValueExpr
{
    public double val;

    public RealExpr(double val) {
        super(-1);
        this.val = val;
    }
    
    public String getTypeName() {
        return "real";
    }
    
    public String toString() {
        return Double.toString(val);
    }
    
    public int hashCode() {
        return (int)val;
    }
    
    public boolean equals(Object obj) {
        return (obj instanceof RealExpr) && ((RealExpr)obj).val == val;
    }
    
}
package webl.page;

import webl.lang.expr.*;
import webl.lang.*;

public class TagExpr extends ValueExpr
{
    public Page page;
    public Tag tag;
    
    public TagExpr(Page page, Tag tag) {
        super(-1);
        this.page = page;
        this.tag = tag;
        tag.addRef();
    }
    
    public String getTypeName() {
        return "tag";
    }
    
    protected void finalize() {
        try {
            tag.releaseRef(page);
        } catch (Exception e) {
            throw new InternalError("Exception in TagExpr.finalize: " + e);
        }
    }
    
    public String toString() {
        return "<A Tag>";
    }
    
    public Expr getPiece() {
        Piece p = tag.getOwner();
        if (p != null)
            return p;
        else
            return Program.nilval;
    }
    
    public boolean equals(Object o) {
        return o instanceof TagExpr && ((TagExpr)o).tag == tag;
    }
}
package webl.page;

import webl.lang.*;
import webl.lang.expr.*;

public class PIPiece extends Piece implements Cloneable
{
    String target;
    
    public PIPiece(Page p, String target, String content) {
        super(p);
        this.name = "?" + target;
        this.target = target;
        setAttr("content", content);
    }    
    
    private PIPiece(Page p) {
        super(p);
    }    
    
    public Object clone() {
        Piece p = new PIPiece(page);
        Piece.Copy(this, p);
        return p;
    }    
    
    public void writeOpenTag(StringBuffer buf) {
        buf.append("<?").append(target).append(" ").append(getAttr("content")).append("?>");
    }
    
    public void writeCloseTag(StringBuffer buf) {
        throw new Error("illegal use");
    }    
}
package webl.page;

import webl.lang.*;
import webl.lang.expr.*;

public class DoctypePiece extends Piece implements Cloneable
{
    public DoctypePiece(Page p, String content) {
        super(p);
        this.name = "!DOCTYPE";
        setAttr("content", content);
    }    
    
    private DoctypePiece(Page p) {
        super(p);
    }    
    
    public Object clone() {
        Piece p = new DoctypePiece(page);
        Piece.Copy(this, p);
        return p;
    }    
    
    public void writeOpenTag(StringBuffer buf) {
        buf.append("<!DOCTYPE").append(" ").append(getAttr("content")).append(">");
    }
    
    public void writeCloseTag(StringBuffer buf) {
        throw new Error("illegal use");
    }    
}
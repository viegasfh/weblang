package webl.page;

import webl.lang.*;
import webl.lang.expr.*;

public class CommentPiece extends Piece implements Cloneable
{
    public CommentPiece(Page p, String comment) {
        super(p);
        this.name = "!--";
        setAttr("comment", comment);;
    }    
    
    private CommentPiece(Page p) {
        super(p);
    }    
    
    public Object clone() {
        Piece p = new CommentPiece(page);
        Piece.Copy(this, p);
        return p;
    }
    
    public void writeOpenTag(StringBuffer buf) {
        buf.append("<!--").append(getAttr("comment")).append("-->");
    }
    
    public void writeCloseTag(StringBuffer buf) {
        throw new InternalError("illegal use");
    }    
}

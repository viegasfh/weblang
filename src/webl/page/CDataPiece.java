package webl.page;

import webl.lang.*;
import webl.lang.expr.*;

public class CDataPiece extends Piece implements Cloneable
{
    public CDataPiece(Page p, String content) {
        super(p);
        this.name = "![CDATA[";
        setAttr("content", content);
    }    
    
    private CDataPiece(Page p) {
        super(p);
        this.name = "![CDATA[";
    }    
    
    public Object clone() {
        Piece p = new CDataPiece(page);
        Piece.Copy(this, p);
        return p;
    }
    
    public void writeOpenTag(StringBuffer buf) {
        buf.append("<![CDATA[").append(getAttr("content")).append("]]>");
    }
    
    public void writeCloseTag(StringBuffer buf) {
        throw new Error("illegal use");
    }    
}
package webl.page;

public class JSONPiece extends Piece {
  public JSONPiece(Page p) {
    super(p);
  }

  // make a named piece
  public JSONPiece(Page p, String name) {
      super(p, name);
  }

  public void writeOpenTag(StringBuffer buf) {    
    /*if (name != null) {
        buf.append("<").append(name);
        
        Enumeration e = EnumKeys();
        while(e.hasMoreElements()) {
            Expr n = (Expr)e.nextElement();
            Expr val = (Expr)get(n);
            if (!(val instanceof AbstractMethExpr)) {   // methods are not attributes
                buf.append(" ").append(n.print());
                if (page.format == Page.XML || !EmptyAttr(val)) {
                    buf.append("=\"");
                    appendAttrVal(buf, val.print());
                    buf.append("\"");
                }
            }
        }
        if (end == beg && page.format == Page.XML)
            buf.append("/>");
        else
            buf.append(">");           
    }*/
  }

  public void writeCloseTag(StringBuffer buf) {    
      /*if (name != null) {
          buf.append("</").append(name).append(">");
      }*/
  }
}
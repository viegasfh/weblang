package webl.page;

import webl.lang.*;
import webl.lang.expr.*;
import webl.util.*;
import java.util.*;
import com.oroinc.text.regex.*;

public class Piece extends ObjectExpr implements Cloneable
{
    public Tag      beg, end;     // begin and end tags
    public Page     page;         // page this piece belongs to
    public String   name;         // piece name
    
    // make an anonymous piece
    public Piece(Page p) {
        page = p;
    }

    // make a named piece
    public Piece(Page p, String name) {
        this(p);
        this.name = name;
    }
    
    protected void finalize() {
        try {
            beg.releaseRef(page);
            end.releaseRef(page);
        } catch (Exception e) {
            throw new InternalError("Exception in Piece.finalize: " + e);
        }
    }
    
    protected static void Copy(Piece source, Piece dest) {
        ObjectExpr.Copy(source, dest);
        dest.name = source.name;
    }
    
    public Object clone() {
        Piece p = new Piece(page);
        Copy(this, p);
        return p;
    }
    
    public String getTypeName() {
        return "piece";
    }
    
    public void setBeg(Tag t) {
        if (beg != null)
            throw new Error("cannot set begin tag");
        beg = t;
        t.addRef();
    }
    
    public void setEnd(Tag t) {
        if (end != null)
            throw new Error("cannot set end tag");
        end = t;
        t.addRef();
    }
    
    public void setAttr(String name, String value) {
        def(name, Program.Str(value));
    }
    
    public String getAttr(String name) {
        Expr o = (Expr)get(Program.Str(name));
        if (o != null)
            return o.print();
        else
            return null;
    }
    
    private void appendAttrVal(StringBuffer buf, String val) {
        for(int i = 0; i < val.length(); i++) {
            char ch = val.charAt(i);
            if (ch == '"')
                buf.append("&quot;");
            else
                buf.append(ch);
        }
    }
    
    public void writeOpenTag(StringBuffer buf) {    
        if (name != null) {
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
        }
    }
    
    public void writeCloseTag(StringBuffer buf) {    
        if (name != null) {
            buf.append("</").append(name).append(">");
        }
    }
    
    boolean EmptyAttr(Expr e) {
        return (e instanceof StringExpr && ((StringExpr)e).val().equals(""));
    }    
    
    public String getText() {
        return page.getText(this);                 // synchronized
    }
    
    public String getMarkup() {
        return page.getMarkup(this);                 // synchronized
    }
    
    public String getPrettyMarkup() {
        return page.getPrettyMarkup(this);                 // synchronized
    }
    
    public PieceSet Children() {
        try {
            return page.Children(this);
        } catch (TypeCheckException e) {
            throw new Error("internal error");
        }
    }
    
    public Piece Parent() {
        try {
            return page.Parent(this);
        } catch (TypeCheckException e) {
            throw new Error("internal error");
        }
    }
    
    //
    // piece-wise comparison methods
    // 
    
    static boolean equal(Piece x, Piece y) {
        return x.beg.sno == y.beg.sno && x.end.sno == y.end.sno;
    }
    
    static boolean inorder(Piece x, Piece y) {
        return x.beg.sno < y.beg.sno || (x.beg.sno == y.beg.sno && x.end.sno <= y.end.sno);
    }
    
    // reverse of beg, end sort
    static boolean inrevorder(Piece x, Piece y) {
        return x.end.sno < y.end.sno || (x.end.sno == y.end.sno && x.beg.sno <= y.beg.sno);
    }
    
    static boolean contain(Piece x, Piece y) {
        return x.beg.sno <= y.beg.sno && y.end.sno <= x.end.sno && !equal(x, y);
    }
    
    static boolean in(Piece x, Piece y) {
        return contain(y, x);
    }    
    
    static boolean overlap(Piece x, Piece y) {
        return !(x.beg.sno > y.end.sno || x.end.sno < y.beg.sno && !equal(x, y));
    }
    
    // x is completely before y
    static boolean cbefore(Piece x, Piece y) {
        return x.end.sno < y.beg.sno;
    }    
    
    // x is completely after y
    static boolean cafter(Piece x, Piece y) {
        return x.beg.sno > y.end.sno;
    }    
    
    static boolean before(Piece x, Piece y) {
        return x.beg.sno < y.beg.sno;
    }
    
    static boolean after(Piece x, Piece y) {
        return x.beg.sno > y.beg.sno;
    }
    
    static boolean endsafter(Piece x, Piece y) {
        return x.end.sno > y.end.sno;
    }
    
}




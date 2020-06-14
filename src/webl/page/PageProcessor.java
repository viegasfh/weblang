package webl.page;

public interface PageProcessor 
{
    public void Text(Str s);
    public void BeginTag(Piece p);
    public void EndTag(Piece p);
    public void BeginEndTag(Piece p);
}
package webl.page;

public interface TextChopper
{
    public boolean nextTag();
    
    public int getPosition();
    public boolean isBeginTag();
    public boolean isEndTag();
    public String getName();
    public Object getHandle();
    public Object getOtherHandle();
}

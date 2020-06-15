package webl.dtd;

public class DTDAttribute
{
    public String name;
    public Object type;            // either a string or a set of strings
    public String defval;
    public boolean fixed;
    
    public DTDAttribute(String name, Object type, String defval, boolean fixed) {
        this.name = name;
        this.type = type;
        this.defval = defval;
        this.fixed = fixed;
    }
}

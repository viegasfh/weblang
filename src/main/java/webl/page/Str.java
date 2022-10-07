package webl.page;

public class Str extends Elem
{
    private String  pcdata;         // character data
    
    public Str(String s) {
        pcdata = s;
        charwidth = s.length();
    }
    
    final public String getPCData() {
        return pcdata;
    }
    
    final public void setPCData(String s) {
        pcdata = s;
        charwidth = s.length();
    }    
}

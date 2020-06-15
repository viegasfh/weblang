package webl.util;

public class StringLog extends Logger
{
    public boolean debug;
    private StringBuffer s = new StringBuffer();
    private String eol = System.getProperty("line.separator");
    
    public StringLog(boolean debug) {
        this.debug = debug;
    }
    
    public void println(String msg) {
        super.println(msg);
        s.append(msg).append(eol);
    }
    
    public void print(String msg) {
        super.print(msg);
        s.append(msg);
    }
    
    public void debugln(String msg) {
        super.debugln(msg);
        if (debug)
            s.append(msg).append(eol);
    }    
    
    public String toString() {
        return s.toString();
    }
    
    public void Clear() {
        s = new StringBuffer();
    }
}

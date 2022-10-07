package webl.util;

public class ConsoleLog extends Logger
{
    public boolean debug;
    
    public ConsoleLog(boolean debug) {
        this.debug = debug;
    }
    
    public void println(String msg) {
        super.println(msg);
        System.out.println(msg);
    }
    
    public void print(String msg) {
        super.print(msg);
        System.out.print(msg);
    }
    
    public void debugln(String msg) {
        super.debugln(msg);
        if (debug)
            System.out.println(msg);
    }    
}

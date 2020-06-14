package webl.util;

import java.io.*;

//
// Empty logger that does nothing.
//

public class Logger
{
    protected Logger chain = null;
    
    public Logger() {
    }
    
    public void ChainTo(Logger chain) {
        this.chain = chain;
    }
    
    public void println(String msg) {
        if (chain != null)
            chain.println(msg);
    }
    
    public void print(String msg) {
        if (chain != null)
            chain.print(msg);
    }
    
    public void debugln(String msg) {
        if (chain != null)
            chain.debugln(msg);
    }
}
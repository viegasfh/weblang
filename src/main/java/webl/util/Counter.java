package webl.util;

import webl.util.*;
import java.util.*;

public class Counter
{
    static Vector counters = new Vector();
    
    private String name;
    private long sum = 0;
    private long beg;
    static private long starttime;
    
    static public void Init() {
        starttime = System.currentTimeMillis();
    }
    
    static public void Report() {
        long tsum = System.currentTimeMillis() - starttime;
        Log.println("WebL counters:");
        Log.println("*   Total runtime  = " + tsum + "ms");
        Enumeration enum = counters.elements();
        while (enum.hasMoreElements()) {
            Counter c = (Counter)(enum.nextElement());
            long p = c.sum * 100 / tsum;
            Log.println("*      " + c.name + "  " + c.sum + "ms (" + p + "%)");
        }
    }
    
    public Counter(String name) {
        this.name = name;
        counters.addElement(this);
    }
    
    public void begin() {
        beg = System.currentTimeMillis();
    }
    
    public void end() {
        sum += System.currentTimeMillis() - beg;
    }
}

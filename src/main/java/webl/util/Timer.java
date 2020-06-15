package webl.util;

public class Timer
{
    private long time;
    
    public void Start() {
        time = System.currentTimeMillis();
    }
    
    public void Report(String msg) {
        long t = System.currentTimeMillis();
        Log.debugln("[" + msg + " " + (t - time) + "ms]");
        time = t;
    }
    
    public long Time() {
        long t = System.currentTimeMillis();
        long res = t - time;
        time = t;
        return res;
    }
}
package weblx.browser;

public class Win32 {
    
    public native String DDERequest(String application, String topic, String item) throws IllegalArgumentException;
    public native void ShellExec(String url);
    
    static {
        System.loadLibrary("weblwin32");
    }
}
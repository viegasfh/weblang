package webl.util;

import java.io.*;

public class Log
{
    static private Logger logger = null;
    
    static public Logger GetLogger() {
        return logger;
    }
    
    static public void SetLogger(Logger output) {
        logger = output;
    }
    
    static public void println(String msg) {
        logger.println(msg);
    }
    
    static public void print(String msg) {
        logger.print(msg);
    }
    
    static public void debugln(String msg) {
        logger.debugln(msg);
    }
}
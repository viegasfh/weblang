package webl.util;

import java.io.*;
import java.util.*;

public class FileLog extends Logger
{
    public boolean          logprint, logdebug;
    private FileWriter      log = null;
    private String          filename;
    private String          eol;
    
    public FileLog(String filename, boolean logprint, boolean logdebug) throws IOException {
        this.filename = filename;
        this.logprint = logprint;
        this.logdebug = logdebug;
        
         eol = System.getProperty("line.separator");
    	 log = new FileWriter(filename);
    }
    
    public void println(String msg) {
        super.println(msg);
        if (logprint) {
          try {
              log.write(msg);
    	      log.write(eol);
    	      log.flush();
	      } catch (IOException e) {
	          System.out.println("IOException writting log file " + e);
	          System.exit(1);
	      }
	    }
    }
    
    public void print(String msg) {
        super.print(msg);
        if (logprint) {
          try {
              log.write(msg);
    	      log.flush();
	      } catch (IOException e) {
	          System.out.println("IOException " + e);
	          System.exit(1);
	      }
	    }
    }
    
    public void debugln(String msg) {
        super.debugln(msg);
        if (logdebug) {
          try {
              log.write(msg);
    	      log.write(eol);
    	      log.flush();
	      } catch (IOException e) {
	          System.out.println("IOException writing log " + e);
	          System.exit(1);
	      }            
        }
    }   
    
}
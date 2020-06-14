
import java.io.*;
import java.util.*;
import webl.util.*;
import webl.lang.*;
import webl.lang.expr.*;
import webl.util.*;

public class WebL
{
    static boolean pause = false, counters = false;
    
    static public void Usage(String msg) {
        System.out.println(msg);
        System.out.println();
        System.out.println("WebL 3.0h (alpha)");
        System.out.println("Usage: webl {options} scriptname {args}");
        System.out.println("Options:");
        System.out.println("  -D         Print debugging output to console");
        System.out.println("  -Llogfile  Log debugging output to file");
        System.out.println("  -P         Pause at exit waiting for a keystroke");
        System.out.println("  -C         Print performance counters at finish");
        System.out.println();
        System.exit(1);
    }

    static private void Pause() {
        if (pause) {        
            System.out.println("Hit ENTER to finish ...");
            try {
                System.in.read();
            } catch (IOException e) {
            }
        }    
    }
    
	static public void main(String args[]) {	
	    Machine machine = null;
	        
	    Counter.Init();
	    if (args.length < 1) 
	        Usage("No script specified");
        
        int arg0 = 0;
        
        boolean debug = false;
        String  debugfile = null;
        
        // check for options
        String arg = args[arg0];
        while (arg.charAt(0) == '-') {
            if (arg.length() < 2) Usage("Illegal option " + arg);
            
            switch (arg.charAt(1)) {
                case 'D':
                    debug = true;
                    break;
                case 'L':
                    debugfile = args[arg0].substring(2);
                    break;
                case 'P':
                    pause = true;
                    break;
                case 'C':
                    counters = true;
                    break;
                default:
                    Usage("Illegal option " + arg);
            }
            arg = args[++arg0];
        }
        
        // set up the default output logs
        try {
            Logger logger = new ConsoleLog(debug);               
            if (debugfile != null)
                logger.ChainTo(new FileLog(debugfile, false, true));
                
            Log.SetLogger(logger);
    	} catch (IOException e) {
    	    System.out.println("An IOException ocurred trying to create file " + debugfile + "," + e);
    	    System.exit(1);
    	}
    	
	    // Load properties
        try {
            LoadProperties("webl.properties");
    	} catch (IOException e) {
    	    Log.println("An IOException ocurred while reading webl.properties, " + e);
    	    System.exit(1);
    	}                 
        
        // allocate a fresh execution machine
	    try {
	        machine = new Machine("Startup.webl");
        } catch (FileNotFoundException e) {
            Log.println("Panic: Unable to locate file, " + e);
            System.exit(1);
        } catch (IOException e) {
            Log.println("Panic: Error while running Startup.webl, " + e);
            System.exit(1);
        } catch (WebLException e) {
            Log.println("Panic: exception while running Startup.webl, " + e.report());
            System.exit(1);
        }
        
        machine.SetARGS(args, arg0);
        
	    try {
            // run the actual script
            InputStream in = null;
            try {
                in = FileLocator.Find(args[arg0]);
                if (in != null) {
                    AutoStreamReader s = new AutoStreamReader(in, "", "");
                    BufferedReader di = new BufferedReader(s);
                    machine.Exec(args[arg0], di);
                } else {
                    Log.println("File not found: " + args[arg0]);
                    Pause();
                    System.exit(1);
                }
            } finally {
                if (in != null)
                    in.close();
            }
        } catch (IOException e) {
            Log.println("IOException " + e);
            e.printStackTrace();
            Pause();
            System.exit(1);
        } catch (WebLException e) {
            Log.println(e.report());
            Pause();
            System.exit(1);
        } catch (WebLReturnException e) {
            Log.println("A return statement was executed outside of a function or method, " + e);
            Pause();
            System.exit(1);
        }
        
        if (counters) Counter.Report();
        Pause();
        System.exit(0);
	}	
	
	static public void LoadProperties(String name) throws IOException {
	    Properties props = new Properties(System.getProperties());
        InputStream in = FileLocator.Find(name);
        if (in != null) {
            props.load(new BufferedInputStream(in));
            System.setProperties(props);
        }
	}

}

/*
Class to illustrate how to call the WebL interpreter from a 
Java program. First a new webl.lang.Machine is created with the
default WebL initialization script called "Startup.webl", the
latter which is typically located in the /scripts directory inside 
WebL.jar.

Each call to Exec of the machine runs a script in a fresh context. 
This means that the side-effects of Exec calls are not visible between
calls to the Exec method. If you need to have the side effects visible
between calls, see CallWebL2 for details.
*/

import webl.lang.*;
import webl.lang.expr.*;

import java.io.*;
import java.util.*;

public class CallWebL
{
    static public void main(String[] arg) {
        try {
            Expr R;
            
            // Load webl.properties file
	        Properties props = new Properties(System.getProperties());
            props.load(new BufferedInputStream(new FileInputStream("webl.properties")));
            System.setProperties(props);
            
            // Allocate a fresh WebL execution machine
            Machine M = new webl.lang.Machine("Startup.webl");
            
            // Set the command line arguments of the WebL machine
            M.SetARGS(new String[] {"arg1", "arg2", "arg3"});
            
            // Calculate 1 + 1 and print the result
            R = M.Exec(" 1 + 1 ");
            System.out.println(R.print());
            
            // Calculate ARGS[1] and print the result
            R = M.Exec(" ARGS[1] ");
            System.out.println(R.print());
            
            // Execute the script in file "test.webl" and print the result
            R = M.ExecFile("test.webl");
            System.out.println(R.print());
            
        } catch (IOException e) {
            System.out.println(e.toString());
        } catch (WebLException e) {
            System.out.println(e.report());
        } catch (WebLReturnException e) {
            // A WebL "return" statement was executed outside a function or method
            System.out.println(e.toString());
        }
    }
}

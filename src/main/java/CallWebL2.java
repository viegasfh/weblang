/*
Class to illustrate how scripts can be evaluated incrementally,
with side-effects from evaluation to evaluation. This is 
accomplished with a webl.lang.Evaluator object working on a 
webl.lang.Machine. This execution behavior is useful when embedding
WebL inside other environments, for example when an enclosing
application embeds WebL code inside HTML. If you don't need this
side-effect feature, look at CallWebL.java for details.

Note that multiple Evaluators can work on the same machine 
keeping side-effects separate. This is useful when the same
imported modules are shared by a number of evaluators (the
underlying WebL machine keeps track of the imported module
list).

Note that each call to the Exec method of the Evaluator object puts a fresh
context on the context stack. This implies that you can re-define
previously defined variables (in fact they are shadowed) in each 
Exec call, but you can't redefine the same variable in the same
call to Exec. Also, as the new contexts can never be de-allocated,
you will be paying in memory each time you do an Exec method call.
This eventually translates into slower execution times, as more
contexts have to be traversed for variable resolution and lookup.
A GC of the evaluator will eventually collect all the contexts
associated with it.

Note a call to Exec allows you to pass a line number where
the script is logically starting, so that decent syntax reporting
can be done.
*/

import webl.lang.*;
import webl.lang.expr.*;
import webl.util.*;

import java.io.*;
import java.util.*;

public class CallWebL2
{
    static Machine      machine;
    static Evaluator    evaluator;
    
    static public void Exec(String script, int startinglineno) {
        try {
            Expr R = evaluator.Exec(script, startinglineno);
            if (R == null)
                System.out.println("Syntax error in " + script);
                // Syntax errors are collected in the Logger used in the Evaluator constructor.
            else
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
    
    static public void main(String[] arg) {
        try {
            Expr R;
            
            // Load webl.properties file (contains proxy settings etc)
	        Properties props = new Properties(System.getProperties());
            props.load(new BufferedInputStream(new FileInputStream("webl.properties")));
            System.setProperties(props);
            
            // We would like debugging output to the console
            Log.SetLogger(new ConsoleLog(true));
            
            // Allocate a fresh WebL execution machine
            machine = new webl.lang.Machine("Startup.webl");
            
            // Set the command line arguments of the WebL machine
            machine.SetARGS(new String[] {"arg1", "arg2", "arg3"});
            
            // Allocate the evaluator which we will "feed" the script to incrementally.
            // Note: An optional Logger can be passed to the Evaluator to catch syntax
            // errors during script compilation,
            evaluator = new Evaluator(machine, "MyModule");
            
            Exec("1+1", 1);
            
            // Define a function
            Exec("var fact = fun(n) if n == 1 then 1 else n * fact(n-1) end end", 2);
            
            // Call the defined function
            Exec("fact(4)", 3);
            
            Exec("PrintLn(\"Hello World\")", 4);
            
            Exec("import Browser; Browser_ShowPage(\"abc\")", 5);
            
            // Redefine the fact function (see explanation at top of file)
            Exec("var fact = fun(n) var fact = 42; fact end", 6);
            
            Exec("PrintLn(\"Hello Again\")", 7);
            
            // Call the (re)-defined function
            Exec("fact(4)", 8);
            
            // Redefine the fact function again and attempt to change immediately
            // afterwords (will cause an exception; see explanation at top of file)
            Exec("var fact = fun(n) n end; var fact = 2", 9);            
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

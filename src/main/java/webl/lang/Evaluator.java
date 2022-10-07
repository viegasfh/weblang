package webl.lang;

import webl.util.*;
import webl.lang.expr.*;
import java.io.*;

// See CallWebL2.java for usage instructions.

public class Evaluator
{
    public Machine      machine;
    public String       modname;
    public Logger       logger;
    
    public Context      topContext;
    public Scope        topScope;
    
    public Evaluator(Machine machine, String modname) {
        this.machine = machine;
        this.modname = modname;
        this.logger = Log.GetLogger();
        
        topScope = machine.universeScope;
        topContext = machine.universe;        
    }
    
    // Logger will catch all the syntax errors while evaluating scripts using this
    // evaluator.
    public Evaluator(Machine machine, String modname, Logger logger) {
        this.machine = machine;
        this.modname = modname;
        this.logger = logger;
        
        topScope = machine.universeScope;
        topContext = machine.universe;        
    }
    
    public Expr Exec(Reader prog, int lineno) throws WebLException, IOException {
        Scanner S = new Scanner(modname, prog, logger, lineno);
        Parser P = new Parser();
        
        Scope oldS = topScope;
        
        topScope = new Scope(topScope);
        Expr n = P.Parse(machine, topScope, S, logger);
        
        if (P.getErrorCount() != 0) {
            topScope = oldS;        // undo any changes that might have taken place
            return null;
        } else {
            topContext = new Context(topContext, null, topScope, Program.Str("--stack bottom--"));
            return n.eval(topContext);
        }
    }    
    
	public Expr Exec(String script, int lineno) throws IOException, WebLException, WebLReturnException {
        BufferedReader di = new BufferedReader(new StringReader(script));
        return Exec(di, lineno);
	}    
}
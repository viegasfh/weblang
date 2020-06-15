package webl.lang;

import java.util.*;
import java.io.*;
import webl.lang.expr.*;
import webl.util.*;

/**
WebL execution machine that sets up a default execution context,
accepts WebL programs, and parses and executes them.
*/
public class Machine
{
    /** Universe of predefined functions and variables. */
    public Context      universe;
    public Scope        universeScope;

    /** Loaded modules, (string, module) pairs */
    public Hashtable modules = new Hashtable();

    private webl.util.Set modulesloading = new webl.util.Set();

    public Machine(String startupscript) throws WebLException, IOException {
  	    if (Log.GetLogger() == null)
    	    Log.SetLogger(new ConsoleLog(false));        // console logger

        InputStream in = FileLocator.Find(startupscript);
        if (in != null) {
            AutoStreamReader s = new AutoStreamReader(in, "", "");
            BufferedReader di = new BufferedReader(s);
            Init(startupscript, di, null, 0, Log.GetLogger());
         } else
            throw new FileNotFoundException(startupscript);
    }

    public Machine(String modname, Reader startup, String[] args, int arg0, Logger logger) throws WebLException, IOException {
        Init(modname, startup, args, arg0, logger);
    }

    protected void Init(String modname, Reader startup, String[] args, int arg0, Logger logger) throws WebLException, IOException {
        Expr n = null;

        universeScope = Program.NewUniverseScope(this);

        if (startup != null) {
            Scanner S = new Scanner(modname, startup, logger, 1);
            Parser P = new Parser();
            n = P.Parse(this, universeScope, S, logger);
            if (P.getErrorCount() != 0)
                throw new IOException("syntax error in startup script");
        }

        universe = Program.NewUniverse(this, null, universeScope);

        SetARGS(args, arg0);

        // set up the property object
        ObjectExpr O = new ObjectExpr();
        Properties prop = System.getProperties();
        Enumeration enumeration = prop.propertyNames();
        while (enumeration.hasMoreElements()) {
            String propname = (String)enumeration.nextElement();
            O.def(propname, Program.Str(prop.getProperty(propname)));
        }
        universe.assign("PROPS", O);

        if (n != null)
            n.eval(universe);
    }

    public void SetARGS(String[] args) {
        SetARGS(args, 0);
    }

    public void SetARGS(String[] args, int arg0) {
        ListExpr A = new ListExpr();
        if (args != null) {
            for (int i = arg0; i < args.length; i++) {
                A = A.Append(Program.Str(args[i]));
            }
        }
        universe.assign("ARGS", A);
    }

	public Expr ExecFile(String filename) throws IOException, FileNotFoundException, WebLException, WebLReturnException {
	    InputStream in = FileLocator.Find(filename);
	    if (in == null)
	        throw new FileNotFoundException(filename);
	    AutoStreamReader s = new AutoStreamReader(in, "", "");
        BufferedReader di = new BufferedReader(s);
        return Exec(filename, di);
	}

	public Expr Exec(String script) throws IOException, WebLException, WebLReturnException {
        BufferedReader di = new BufferedReader(new StringReader(script));
        return Exec(script, di);
	}

    public Expr Exec(String modname, Reader prog) throws WebLException, IOException, WebLReturnException {
        return Exec(modname, prog, Log.GetLogger());
    }

    public Expr Exec(String modname, Reader prog, Logger logger) throws WebLException, IOException, WebLReturnException {
        Scanner S = new Scanner(modname, prog, logger, 1);
        Parser P = new Parser();

        Scope scope = new Scope(universeScope);
        Expr n = P.Parse(this, scope, S, logger);

        if (P.getErrorCount() != 0)
            return null;
        else {
            Context top = new Context(universe, null, scope, Program.Str("--stack bottom--"));
            return n.eval(top);
        }
    }

    public synchronized Module findModule(String modname) {
        Object m = modules.get(modname);
        if (m != null)
            return (Module)m;
        else
            return null;
    }

    public synchronized Module loadModule(String modname, Logger logger) throws FileNotFoundException, IOException, WebLException {
        return loadModule(modname, false, logger);
    }

    public synchronized Module loadModule(String modname, boolean reloadifchanged, Logger logger) throws FileNotFoundException, IOException, WebLException {
        long[] lastmodified = {0};

        Module m = findModule(modname);
        if (m != null) {      // already loaded
            // check if the last modified date of the module file has changed since the module
            // was first load, and if so, remove the module from the module list, and reload
            // the module.
            if (reloadifchanged) {
                InputStream in = FileLocator.Find(modname + ".webl", lastmodified);
                if (in == null)     // module has been deleted
                    throw new FileNotFoundException(modname + ".webl");

                if (lastmodified[0] != m.lastmodified) {    // module has changed
                    logger.debugln("[unloading module " + modname + "]");
                    modules.remove(modname);
                    return loadModule(modname, false, logger);
                }
            }

            return m;
        } else {
            if (modulesloading.contains(modname)) {         // recursive loading
                throw new IOException("module cycle involving module " + modname);
            }
            try {
                modulesloading.put(modname);

                InputStream in = FileLocator.Find(modname + ".webl", lastmodified);
                if (in == null)
                    throw new FileNotFoundException(modname + ".webl");

                AutoStreamReader is = new AutoStreamReader(in, "", "UTF8");
                BufferedReader ds = new BufferedReader(is);

                logger.debugln("[parsing module " + modname + "]");

                Scanner S = new Scanner(modname + ".webl", ds, logger, 1);
                Parser P = new Parser();
                Scope modscope = new Scope(universeScope);
                Expr n = P.Parse(this, modscope, S, logger);

                if (P.getErrorCount() != 0) {
                    return null;
                } else {
                    m = new Module(this, modname, modscope, lastmodified[0]);
                    modules.put(modname, m);  // insert into module this before evaluating

                    logger.debugln("[running module body " + modname + "]");
                    n.eval(m.context);

                    return m;
                }
            } finally {
                modulesloading.remove(modname);
            }
        }
    }
}

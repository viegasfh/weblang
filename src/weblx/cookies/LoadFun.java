package weblx.cookies;

import webl.lang.*;
import webl.lang.expr.*;
import webl.lang.builtins.*;
import webl.page.*;
import webl.page.net.*;
import weblx.url.MalformedURL;

import java.io.*;
import java.util.*;

public class LoadFun extends AbstractFunExpr
{
    public String toString() {
        return "<Cookies_Load>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        if (args.size() < 1 || args.size() > 2)
            throw new WebLException(c, callsite, "ArgumentError", this + " function expects one or two arguments");
        
        String filename = StringArg(c, args, callsite, 0);
        String dbid = null;
        if (args.size() == 2) 
            dbid = StringArg(c, args, callsite, 1);
            
        try {
            File F = new File(filename);
            Reader R = new InputStreamReader(new FileInputStream(F));
            Net.getCookieDB(dbid).Load(R);
            R.close();
            return Program.nilval;
        } catch(IllegalCookieException e) {
            throw new WebLException(c, callsite, "BadCookie", "bad cookie in file " + filename + ": " + e);
        } catch(FileNotFoundException e) {
            throw new WebLException(c, callsite, "FileNotFound", "unable to locate " + filename);
        } catch(IOException e) {
            throw new WebLException(c, callsite, "IOException", "unable to read " + filename);
        } catch (MalformedURL e) {
            throw new WebLException(c, callsite, "MalformedURL", "bad cookie URL in file " + filename + ": " + e);
        }
    }
}
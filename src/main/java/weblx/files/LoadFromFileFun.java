package weblx.files;

import webl.lang.*;
import webl.lang.expr.*;
import webl.lang.builtins.*;
import webl.page.*;
import webl.page.net.*;

import java.io.*;
import java.util.*;

public class LoadFromFileFun extends AbstractFunExpr
{
    public String toString() {
        return "<LoadFromFile>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);
        String filename = StringArg(c, args, callsite, 0);
        String mimetype = StringArg(c, args, callsite, 1);
        
        try {
            File F = new File(filename);
            FileInputStream S = new FileInputStream(F);
            return Net.FetchPage(S, "", mimetype, null);
        } catch(FileNotFoundException e) {
            throw new WebLException(c, callsite, "FileNotFound", "unable to locate " + filename);
        } catch(NetException e) {
            throw e.makeWebLException(c, callsite, e);
        } catch(IOException e) {
            throw new WebLException(c, callsite, "IOException", "unable to read " + filename);
        }
    }
}
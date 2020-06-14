package weblx.files;

import webl.lang.*;
import webl.lang.expr.*;
import webl.lang.builtins.*;
import webl.page.net.*;
import java.io.*;
import java.util.*;

public class LoadStringFromFileFun extends AbstractFunExpr
{
    public String toString() {
        return "<LoadStringFromFile>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        String filename, enc;
        
        if (args.size() == 1) {
            filename = StringArg(c, args, callsite, 0);
            enc = System.getProperty("file.encoding");
        } else if (args.size() == 2) {
            filename = StringArg(c, args, callsite, 0);
            enc = MIMEType.UnAliasCharset(StringArg(c, args, callsite, 1));
        } else
            throw new WebLException(c, callsite, "ArgumentError", "wrong number of arguments, " 
                + this + " function expects two or three arguments");
        
        try {
            InputStream S = new FileInputStream(filename);
            Reader R;
            if (enc != null && !enc.equals("")) 
                R = new BufferedReader(new InputStreamReader(S, enc));
            else 
                R = new BufferedReader(new InputStreamReader(S));
                
            StringBuffer buf = new StringBuffer();
            int ch;
            while ( (ch = R.read()) != -1) 
                buf.append((char)ch);
    	    return Program.Str(buf.toString());
        } catch(FileNotFoundException e) {
            throw new WebLException(c, callsite, "FileNotFound", "unable to locate " + filename);
        } catch(UnsupportedEncodingException E) {
            throw new WebLException(c, callsite, "UnsupportedEncoding", "Unsupported encoding " + enc);
        } catch(IOException e) {
            throw new WebLException(c, callsite, "IOException", "unable to read " + filename + ", " + e);
        }
    }
}
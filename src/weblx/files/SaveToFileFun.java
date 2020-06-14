package weblx.files;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import webl.page.net.*;
import webl.util.*;
import java.util.*;
import java.io.*;

public class SaveToFileFun extends AbstractFunExpr
{
    public String toString() {
        return "<SaveToFile>";
    }
    
    static public void SaveToFile(String filename, String data, String enc) throws IOException {
        FileOutputStream f = new FileOutputStream(filename);
	    OutputStreamWriter os;
	    
	    if (enc != null && !enc.equals(""))
	        os = new OutputStreamWriter(f, enc);
	    else
	        os = new OutputStreamWriter(f);

	    os.write(data, 0, data.length());
	    os.flush();
	    os.close();
	    // f.close();       // Is a problem on UNIX with Sanjays runtime. So rely on GC to close file.
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        String enc;
        
        if (args.size() == 3) {
            enc = MIMEType.UnAliasCharset(StringArg(c, args, callsite, 2));
        } else if (args.size() == 2) {
            enc = System.getProperty("file.encoding");       // use the default encoding for this platform
        } else
            throw new WebLException(c, callsite, "ArgumentError", "wrong number of arguments, " 
                + this + " function expects two or three arguments");
        
        String fname = StringArg(c, args, callsite, 0);
        Expr e = ((Expr)args.elementAt(1)).eval(c);
        String result = e.print();
        
        try {
            SaveToFile(fname, result, enc);
        } catch(IllegalArgumentException E) {
            throw new WebLException(c, callsite, "UnsupportedEncoding", "Unsupported encoding " + enc);
        } catch(UnsupportedEncodingException E) {
            throw new WebLException(c, callsite, "UnsupportedEncoding", "Unsupported encoding " + enc);
        } catch(IOException E) {
            throw new WebLException(c, callsite, "SaveError", "unable to write output to " + fname + ", " + E);
        }
        
        return Program.nilval;
    }
}

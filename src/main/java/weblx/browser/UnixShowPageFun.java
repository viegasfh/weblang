package weblx.browser;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import webl.page.net.*;
import webl.util.*;
import java.util.*;
import java.io.*;

public class UnixShowPageFun extends AbstractFunExpr
{
    public String toString() {
        return "<ShowPage>";
    }
    
    static public void SaveToFile(String filename, String data, String enc) throws IOException, UnsupportedEncodingException {
        FileOutputStream f = new FileOutputStream(filename);
        OutputStreamWriter os;
        if (enc != null)
    	    os = new OutputStreamWriter(f, enc);
    	else
    	    os = new OutputStreamWriter(f);

	    os.write(data, 0, data.length());
	    os.flush();
	    os.close();
	    //f.close();
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        String enc;
        
        if (args.size() == 2) {
            enc = MIMEType.UnAliasCharset(StringArg(c, args, callsite, 1));
        } else if (args.size() == 1) {
            enc = System.getProperty("file.encoding");       // use the default encoding for this platform
        } else
            throw new WebLException(c, callsite, "ArgumentError", "wrong number of arguments, " 
                + this + " function expects one or two arguments");
            
        
        Expr e = ((Expr)args.elementAt(0)).eval(c);
        
        String result = e.print();
        String fname = TempFileName();
        
        try {
            SaveToFile(fname, result, enc);
            Runtime R = java.lang.Runtime.getRuntime();
            R.exec("netscape -remote openURL(file:" + fname + ")");
        } catch(IllegalArgumentException E) {
            throw new WebLException(c, callsite, "ShowPageError", "Unsupported encoding " + enc);
        } catch(UnsupportedEncodingException E) {
            throw new WebLException(c, callsite, "ShowPageError", "Unsupported encoding " + enc);
        } catch(IOException E) {
            throw new WebLException(c, callsite, "ShowPageError", "Unable to write output to " + fname + ": " + E);
        }
        
        return Program.nilval;
    }
    
    private String TempFileName() {
        String pid = System.getProperty("webl.pid");
        for(int i = 1000; ; i++) {
            String fname = "webl" + i + "." + pid;
            File F = new File("/tmp", fname);
            if (!F.exists())
                return "/tmp/" + fname;
        }
//        throw new Error("unable to create a temporary file");
    }
}


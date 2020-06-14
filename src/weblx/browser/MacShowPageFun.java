/* Added 23rd September 1998 by Steve Marsh to work with Netscape on the Mac.
 * Comments to steve.marsh@iit.nrc.ca.
 *
 * To compile this class you will need MRJClasses.zip from the Macintosh runtime for
 * Java.
 */

package weblx.browser;

import webl.lang.*;
import webl.lang.expr.*;
import webl.page.*;
import webl.page.net.*;
import webl.util.*;
import java.util.*;
import java.io.*;

// Apple MRJ headers
import com.apple.mrj.*;

public class MacShowPageFun extends AbstractFunExpr
{
    public String toString() {
        return "<ShowPage>";
    }
    
    static public void SaveToFile(File fileIn, String data, String enc) throws IOException, UnsupportedEncodingException {
        FileOutputStream f = new FileOutputStream(fileIn);
        OutputStreamWriter os;
        if (enc != null)
            os = new OutputStreamWriter(f, enc);
        else
            os = new OutputStreamWriter(f);

            os.write(data, 0, data.length());
            os.flush();
            os.close();
            f.close();
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
           File macFile = new File(fname);
           if (macFile.exists())
              macFile.delete();
           
           SaveToFile(macFile, result, enc);

           /*Set the file to belong to Netscape */
           MRJFileUtils.setFileTypeAndCreator(macFile, new MRJOSType("TEXT"), new MRJOSType("MOSS"));


           /* Now find Netscape, launch it and tell it to open the file */
           File netscape = MRJFileUtils.findApplication(new MRJOSType("MOSS"));
           String params[] = { netscape.toString(), macFile.toString() };
           java.lang.Runtime.getRuntime().exec(params);
         } catch(IllegalArgumentException E) {
            throw new WebLException(c, callsite, "MacShowPageError", "Unsupported encoding " + enc);
        } catch(UnsupportedEncodingException E) {
            throw new WebLException(c, callsite, "MacShowPageError", "Unsupported encoding " + enc);
        } catch(IOException E) {
            throw new WebLException(c, callsite, "MacShowPageError", "Unable to write output to " + fname + ": " + E);
        } catch (Exception E) {
                throw new WebLException(c, callsite, "MacShowPageError", E.toString());
        }
        
        return Program.nilval;
    }
    
    private String TempFileName() {
        for(int i = 1000; ; i++) {
            String fname = "webl" + i + ".html";
            File F = new File(fname);
            if (!F.exists())
                return fname;
        }
        // throw new Error("unable to create a temporary file");
    }
}
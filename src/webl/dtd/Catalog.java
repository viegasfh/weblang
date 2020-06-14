package webl.dtd;

import java.util.*;
import java.io.*;
import webl.util.*;

public class Catalog
{
    static Hashtable dtdcache = new Hashtable();
    static Hashtable cat = new Hashtable();     // map DTD name to filenames
    
    static protected String findDTD(String dtdname) throws IOException, FileNotFoundException {
        Object fn = cat.get(dtdname);
        if (fn == null) 
            throw new FileNotFoundException(dtdname + " not in catalog");
        else 
            return (String)fn;
    }
    
    synchronized static public String importDTD(String s) throws IOException, FileNotFoundException {
        String fn = findDTD(s);
        try {
            InputStream in = Class.forName("webl.dtd.Catalog").getResourceAsStream(fn);   
            Reader R = new BufferedReader(new InputStreamReader(in));
            
            StringBuffer buf = new StringBuffer();
            int ch;
            while ( (ch = R.read()) != -1) 
                buf.append((char)ch);
            
            return buf.toString();
    	} catch (ClassNotFoundException e) {
    	    throw new RuntimeException("internal error");
    	}
    }
    
    synchronized public static DTD OpenDTD(String name) throws FileNotFoundException, IOException {
        Object obj = dtdcache.get(name);
        if (obj != null)
            return (DTD)obj;
        else {
            try {
                String fn = findDTD(name);
                InputStream in = Class.forName("webl.dtd.Catalog").getResourceAsStream(fn);
            
                Reader R = new BufferedReader(new InputStreamReader(in));
                DTDParser P = new DTDParser(R);
                
                DTD dtd = P.Parse();
                if (dtd != null)
                    dtdcache.put(name, dtd);
                    
                return dtd;
            } catch (ClassNotFoundException e) {
    	        throw new RuntimeException("internal error");
    	    }
        }
    }
    
    public static DTD OpenHTML40() throws FileNotFoundException, IOException {
        return OpenDTD("-//W3C//DTD HTML 4.0//EN");
    }
    
    public static DTD OpenHTML32() throws FileNotFoundException, IOException {
        return OpenDTD("-//W3C//DTD HTML 3.2//EN");
    }
    
    public static DTD OpenHTML20() throws FileNotFoundException, IOException {
        return OpenDTD("-//IETF//DTD HTML Strict Level 1//EN");
    }
    
    static void AddToCat(String name, String filename) {
        cat.put(name, filename);
    }
    
    static {
        try {
            InputStream fi = Class.forName("webl.dtd.Catalog").getResourceAsStream("/dtd/catalog");
            String dir = "/dtd/";
            
            Reader r = new BufferedReader(new InputStreamReader(fi));
            StreamTokenizer st = new StreamTokenizer(r);
            st.quoteChar('\"');
            st.eolIsSignificant(true);
            
            int sym = st.nextToken();
            while (sym != StreamTokenizer.TT_EOF) {
                if (sym == StreamTokenizer.TT_WORD) {
                    // PUBLIC symbol
                    String typ = st.sval;
                    sym = st.nextToken();
                    if (sym == '\"') {
                        // DTD name
                        String dtdname = st.sval;
                        sym = st.nextToken();
                        if (sym == StreamTokenizer.TT_WORD) {
                            // filename
                            String filename = st.sval;
                            sym = st.nextToken();
                            if (sym == StreamTokenizer.TT_EOL) {
                                AddToCat(dtdname, dir + filename);
                            }
                        }
                    }
                } else if (sym == StreamTokenizer.TT_EOL)
                    sym = st.nextToken();
                else if (sym != StreamTokenizer.TT_EOL && sym != StreamTokenizer.TT_EOF) {
                    Log.println("[error on line " + st.lineno() + " in catalog file]");
                    while(sym != StreamTokenizer.TT_EOL && sym != StreamTokenizer.TT_EOF)
                        sym = st.nextToken();
                }
            }
        } catch (FileNotFoundException e) {
            Log.println("[unable to locate catalog file]");
        } catch (IOException e) {
            Log.println("[unable to read catalog file]");
        } catch (ClassNotFoundException e) {
            Log.println("[unable to read catalog file]");
        }        
    }
}

package webl.page.plain;

import java.io.*;
import java.util.*;
import webl.lang.expr.*;
import webl.page.*;
import webl.page.net.*;
import webl.util.*;

public class TextPlainParser implements ParserInterface
{
    private BufReader   R;
    private Page        page;
    
    private final int BUFSIZE = 512;
    private char[] charbuf = new char[BUFSIZE];
    
    public TextPlainParser() {
    }
    
    public String DefaultCharset() {
        return "ISO-8859-1";
    }

    public Page Parse(Reader TR, String url, ObjectExpr options) throws IOException {
        page = new Page(null, Page.HTML);
        this.R = new BufReader(TR);
        
        int pos = 0;
        StringBuffer s = new StringBuffer();
        while(R.ch != -1) {
            if (pos < BUFSIZE) {
                charbuf[pos++] = (char)R.ch;
                R.adv();
            } else {
                s.append(charbuf, 0, pos);
                pos = 0;
            }
        }
        if (pos > 0)
            s.append(charbuf, 0, pos);
        page.appendPCData(s.toString());
        TR.close();
        return page;
    }
    
    void ProcessPCData(String s) {
        page.appendPCData(s);
    }
}

package webl.page;

import java.util.*;
import java.io.*;

public class PageReader extends java.io.Reader
{
    static int  BUFSIZE = 4096;
    Page        page;
    Elem        elem;
    Elem        fin;
    
    String      sbuf = null;
    int         spos = 0;
    
    char[]      charbuf = new char[1];
    char[]      buf = new char[BUFSIZE];
    int         bufbeg = 0, bufend = 0;
    
    public PageReader(Page page) {
        this.page = page;
        elem = page.head.next;
        fin = page.head;
    }
    
    // end not included
    public PageReader(Page page, Elem beg, Elem end) {
        this.page = page;
        this.elem = beg;
        fin = end;
    }    
    
    public int read(char cbuf[], int off, int len) {
        if (bufend == bufbeg) {
            fill();
            if (bufbeg == bufend)
                return -1;
        }
        int n = Math.min(len, bufend - bufbeg);
        System.arraycopy(buf, bufbeg, cbuf, off, n);
        bufbeg += n;
        return n;
    }
    
    public int read() {
	    if (read(charbuf, 0, 1) == -1)
	        return -1;
	    else
	        return charbuf[0];
    }
    
    void fill() {
        bufend = 0;
        bufbeg = 0;
        
        while(bufend < BUFSIZE) {
            if (sbuf != null) {
                int x = sbuf.length() - spos;
                int n = Math.min(BUFSIZE - bufend, x);
                sbuf.getChars(spos, spos + n, buf, bufend);
                bufend += n;
                spos += n;
                if (spos == sbuf.length())
                    sbuf = null;
            } else if (elem == fin)
                return;
            else if (elem.charwidth == 0) 
                elem = elem.next;
            else {
                sbuf = ((Str)elem).getPCData();
                spos = 0;
                elem = elem.next;
            }
        }
    }
    
    public boolean ready() throws IOException {
	    return true;
    }
    
    public void close() {
        elem = page.head;
    }
}


package webl.util;

import java.util.*;
import java.io.*;

public class MacroBufReader
{
    public int  ch;

    Reader          R;
    Hashtable       definitions;
    boolean         expand = true;   // expand macros or not ?
    
    int             buffersize;
    char[]          buf;             // character buffer
    int             bufbeg, bufend;

    int             lineno = 1;

    final int       RECSIZE = 512;      // size of the recording buffer
    StringBuffer    recbuf = null;      // recording buffer
    char[]          recb = new char[RECSIZE];   // tmp recording buffer
    int             recpos;

    public MacroBufReader(Reader R, int buffersize, Hashtable definitions) throws IOException {
        this.R = R;
        this.buffersize = buffersize;
        this.definitions = definitions;
        buf = new char[buffersize];
        bufbeg = bufend = 0;
        ch = read();
        MacroCheck();
    }

    public MacroBufReader(Reader R, Hashtable definitions) throws IOException {
        this(R, 4096, definitions);
    }

    public void startRecording() {
        recbuf = new StringBuffer();
        recpos = 0;
    }

    public String stopRecording() {
        if (recbuf == null)
            return null;

        try {
            if (recpos > 0)
                recbuf.append(recb, 0, recpos);
            return recbuf.toString();
        } finally {
            recbuf = null;
        }
    }

    final private void MacroCheck() throws IOException {
        if (ch == '%' && expand) {
            ch = read();
            if (NameChar(ch)) {
                StringBuffer s = new StringBuffer();
                while(NameChar(ch)) {
                    s.append((char)ch);
                    ch = read();
                }
                if (ch == ';') ch = read();
                
                Object r = definitions.get(s.toString().toLowerCase());
                if (r != null && r instanceof String) {
                    push((String)r);
                    ch = read();
                } // else undefined macro
            } else {                // now we read to much, so push it back on the buffer
                push(String.valueOf((char)ch));
                ch = '%';
            }
        }
    }
    
    // advance to the next character
    final public void adv() throws IOException {
        if (ch == 0xA) lineno++;
        if (recbuf != null) {
            if (recpos < RECSIZE)
                recb[recpos++] = (char)ch;
            else {
                recbuf.append(recb, 0, recpos);
                recpos = 0;
            }
        }
        ch = read();
        MacroCheck();
    }

    // advance to the next character, skip whitespace to the next character
    final public void get() throws IOException {
        adv();
        while (ch == ' ' || ch == 9 || ch == 0xD || ch == 0xA) adv();
    }

    // skip whitespace
    final public void skip() throws IOException {
        while (ch == ' ' || ch == 9 || ch == 0xD || ch == 0xA) adv();
    }
    
    private final int BUFSIZE = 512;
    private char[] charbuf = new char[BUFSIZE];
    
    final public String skipTill(String pat) throws IOException {
        StringBuffer s = new StringBuffer();
        
        int len = pat.length();
        int[] window = new int[len];
        
        // fill the window
        for(int i = 0; i < len; i++) {
            window[i] = ch; adv(); 
        }
        
        int pos = 0;
        while (true) {
            // check if the pattern matches the window
            int i = 0;
            while(i < len && window[i] == pat.charAt(i)) i++;
            if (i == len) {
                if (pos > 0)
                    s.append(charbuf, 0, pos);
                return s.toString();   // pattern matches
            }
            
            if (ch == -1)
                break;
                
            // no match, slide window left one character, use a buffer to reduce sync penalty
            if (pos < BUFSIZE) {
                charbuf[pos++] = (char)window[0];
            } else {
                s.append(charbuf, 0, pos);
                pos = 0;
                charbuf[pos++] = (char)window[0];
            }
            for(i = 0; i < len - 1; i++)
                window[i] = window[i + 1];
            window[len - 1] = ch;       // insert next character at window end
            adv();
        }
        
        if (pos > 0)
            s.append(charbuf, 0, pos);
            
        for(int i = 0; i < len && window[i] != -1; i++)
            s.append((char)window[i]);
        return s.toString();            
                                        
    }

    public int getLine() {
        return lineno;
    }
    
    public void expanding(boolean state) {
        expand = state;
    }

    private int read() throws IOException {
        if (bufend == bufbeg) { // end of this buffer reached
            // check if we have some buffers on the stack
            while (top != null) {
                buffersize = top.buffersize;
                buf = top.buf;
                bufbeg = top.bufbeg;
                bufend = top.bufend;
                ch = top.ch;
                
                top = top.next;
                
                if (bufbeg != bufend)
                    return ch;
            }
            
            if (bufbeg == bufend) {
                // read from the input stream
                bufbeg = bufend = 0;
                while(bufend < buffersize) {
                    int n = R.read(buf, bufend, buffersize - bufend);
                    if (n == -1) break;
                    bufend += n;
                }
            }
            if (bufbeg == bufend)           // still nothing in the buffer
                return -1;
        }
        return buf[bufbeg++];
    }

    final public String readLine() throws IOException{
        StringBuffer s = new StringBuffer();
        while (ch != -1) {
            if (ch == '\r') {
                adv();
                if (ch == '\n') {
                    adv();
                    break;
                } else {
                    break;
                }
            } else if (ch == '\n') {
                adv();
                break; 
            } else {
                s.append((char) ch);
                adv();
            }
        }        
        return s.toString();
    }    
    
    BufState top = null;
    
    private void push(String s) throws IOException {
        // save the current state
        BufState S = new BufState(this);
        S.next = top;
        top = S;
        
        // copy the pushed string into the buffer
        bufbeg = 0;
        buffersize = s.length();
        bufend = buffersize;
        buf = new char[buffersize];
        s.getChars(0, buffersize, buf, 0);
    }
    
    boolean NameChar(int ch) {
        return ch >= 'a' && ch <= 'z'
            || ch >= 'A' && ch <= 'Z'
            || ch >= '0' && ch <= '9'
            || ch == '.'
            || ch == '-' 
            || ch == '_' 
            || ch == ':';
    }
}

class BufState {
    BufState    next;
    int         buffersize;
    char[]      buf;
    int         bufbeg, bufend;
    int         ch;
    
    public BufState(MacroBufReader R) {
        buffersize = R.buffersize;
        buf = R.buf;
        bufbeg = R.bufbeg;
        bufend = R.bufend;
        ch = R.ch;
    }
}
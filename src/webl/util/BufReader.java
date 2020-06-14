package webl.util;

import java.io.*;

public class BufReader
{
    public int  ch;
    
    Reader          R;
    int             buffersize;
    char[]          buf;             // character buffer
    int             bufbeg, bufend;           
    
    int             lineno = 1;
    
    final int       RECSIZE = 512;      // size of the recording buffer
    StringBuffer    recbuf = null;      // recording buffer
    char[]          recb = new char[RECSIZE];   // tmp recording buffer
    int             recpos;
    
    public BufReader(Reader R, int buffersize) throws IOException {
        this.R = R;
        this.buffersize = buffersize;
        buf = new char[buffersize];
        bufbeg = bufend = 0;
        ch = read();    
    } 
    
    public BufReader(Reader R) throws IOException {
        this(R, 4096);
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

    final private boolean CharEq(char x, char y) {
        return Character.toLowerCase(x) == Character.toLowerCase(y);
    }
    
    final public String skipIgnoreCaseTill(String pat) throws IOException {
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
            while(i < len && CharEq((char)window[i], (char)pat.charAt(i))) i++;
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
    
    final private int read() throws IOException {
        if (bufend == bufbeg) {
            bufbeg = bufend = 0;
            while(bufend < buffersize) {
                int n = R.read(buf, bufend, buffersize - bufend);
                if (n == -1) break;
                bufend += n;
            }            
            if (bufbeg == bufend)
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
}
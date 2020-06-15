package webl.util;

import webl.page.*;
import webl.page.net.*;
import java.io.*;
import java.util.*;
import sun.io.ByteToCharConverter;
import sun.io.ConversionBufferFullException;
import sun.io.MalformedInputException;

public class AutoStreamReader extends Reader {
    private ByteToCharConverter     btc;
    private InputStream             in;

    private static final int        defaultByteBufferSize = 8192;
    private byte                    bb[];		/* Input buffer */
    
    private int                     nBytes = 0;	/* -1 implies EOF has been reached */
    private int                     nextByte = 0;
    
    private String                  defaultenc;
    
    /* How encodings are resolved:
    
        if enc != "" then
            use enc
        else    // auto detect
            if unicode signature then
                use "unicode"
            else if stream contains ?XML directive with encoding spec then
                use that encoding
            else 
                use defaultenc
    */
    public AutoStreamReader(InputStream in, String enc, String defaultenc) throws UnsupportedEncodingException {
	    super(in);
	    this.in = in;
	    this.defaultenc = defaultenc;
	    if (enc.equals(""))
	        btc = null;
	    else
    	    btc = ByteToCharConverter.getConverter(MIMEType.UnAliasCharset(enc));
	    bb = new byte[defaultByteBufferSize];
    }

    public String getEncoding() {
	    if (btc != null)
		    return btc.getCharacterEncoding();
	    else
		    return null;
    }

    private boolean inReady() {
    	try {
    	    return in.available() > 0;
    	} catch (IOException x) {
    	    return false;
    	}
    }

    private char cb[] = new char[1];
    
    public int read() throws IOException {
    	if (read(cb, 0, 1) == -1)
    	    return -1;
    	else
    	    return cb[0];
    }

    public int read(char cbuf[], int off, int len) throws IOException {
        int pos = off;
        int end = off + len;
	    
	    while (pos < end) {
	        if (nextByte < nBytes) {
	            try {
    	            pos += btc.convert(bb, nextByte, nBytes, cbuf, pos, end);
    	            nextByte = btc.nextByteIndex();
    	        } catch (ConversionBufferFullException x) {
    		        nextByte = btc.nextByteIndex();
    		        pos = btc.nextCharIndex();
    		    } catch (MalformedInputException x) {
    		        nextByte = btc.nextByteIndex();
    		        pos = btc.nextCharIndex();
    		        Log.debugln("[Warning: Malformed input character sequence ending with 0x" 
    		            + Integer.toHexString(bb[nextByte-1] & 0xFF) + " ignored]");
    		    }
	        } else {
	            if (pos - off > 0 && !inReady()) break;	/* Block */	           
	            
	            try {
        	        nBytes = in.read(bb);
        	    } catch (IOException e) {
        	        nBytes = -1;
        	    }
	            nextByte = 0;

	            if (btc == null)
	                selectEncoder();      // try to determine the encoding
	            
	            if (nBytes == -1) {
                    try {
                        int n = btc.flush(cbuf, pos, end);
                        if (n == 0) return -1;
                        pos += n;
                	} catch (ConversionBufferFullException x) {
                	    nextByte = btc.nextByteIndex();
                	    pos = btc.nextCharIndex();
                	}  catch (MalformedInputException x) {
        		        nextByte = btc.nextByteIndex();
        		        pos = btc.nextCharIndex();
        		        Log.debugln("[Warning: Malformed input character sequence ending with 0x" 
        		            + Integer.toHexString(bb[nextByte-1] & 0xFF) + " ignored]");
        		    }	                
                	break;
	            }
	        }
	    }
	    return pos - off;
    }

/*
   00 00 00 3C: UCS-4, big-endian machine (1234 order) 
   3C 00 00 00: UCS-4, little-endian machine (4321 order) 
   00 00 3C 00: UCS-4, unusual octet order (2143) 
   00 3C 00 00: UCS-4, unusual octet order (3412) 
   FE FF: UTF-16, big-endian 
   FF FE: UTF-16, little-endian 
   00 3C 00 3F: UTF-16, big-endian, no Byte Order Mark (and thus, strictly speaking, in error) 
   3C 00 3F 00: UTF-16, little-endian, no Byte Order Mark (and thus, strictly speaking, in error) 
   3C 3F 78 6D: UTF-8, ISO 646, ASCII, some part of ISO 8859, Shift-JIS, EUC, or any other 7-bit, 8-bit,
   or mixed-width encoding which ensures that the characters of ASCII have their normal positions, width, and
   values; the actual encoding declaration must be read to detect which of these applies, but since all of these
   encodings use the same bit patterns for the ASCII characters, the encoding declaration itself may be read
   reliably 
   4C 6F A7 94: EBCDIC (in some flavor; the full encoding declaration must be read to tell which code page is
   in use) 
   other: UTF-8 without an encoding declaration, or else the data stream is corrupt, fragmentary, or enclosed in a
   wrapper of some kind 
*/
    void selectEncoder() {
        String enc;
        
        byte a = bb[0];
        byte b = bb[1];
        
        if ((a & 0xff) == 0xfe && (b & 0xff) == 0xff)         // UTF-16, big-endian
            enc = "Unicode";
        else if ((a & 0xff) == 0xff && (b & 0xff) == 0xfe)    // UTF-16, little-endian
            enc = "Unicode";
        else                                // UTF-8 or some other encoding as identified by <?xml encoding="x" ...
            enc = parseEncoding();
        
        enc = MIMEType.UnAliasCharset(enc);                 // handle aliasing
        
        if (!enc.equals("")) {
            try {
                btc = ByteToCharConverter.getConverter(enc);
            } catch (IllegalArgumentException x) {
            } catch (UnsupportedEncodingException x) {
            }
        }
        if (btc == null) {
            if (!enc.equals(""))
                Log.debugln("[Cannot locate converter for character set " + enc + ", defaulting to " + defaultenc + "]");
            try {
                btc = ByteToCharConverter.getConverter(MIMEType.UnAliasCharset(defaultenc));
            } catch (UnsupportedEncodingException y) {
                if (!defaultenc.equals(""))
                    Log.debugln("[Cannot locate converter for character set " + defaultenc + ", defaulting to platform convention]");
                btc = ByteToCharConverter.getDefault();
            }
        }
    }
    
    public boolean ready() throws IOException {
        return (nextByte < nBytes) || inReady();
    }

    public void close() throws IOException {
	    if (in != null) {
    	    in.close();
    	    in = null;
    	    bb = null;
    	    btc = null;
    	}
    }

// parsing of the buffer to detect an encoding

    int     getpos = 0;
    int     ch;
    
    private void get() {
        try {
            ch = bb[getpos++];
        } catch (ArrayIndexOutOfBoundsException e) {
            ch = -1;
        }
    }
    
    private void skip() {
        while (ch != -1 && ch <= ' ') get();
    }
    
    private String readname() {
        skip();
        StringBuffer s = new StringBuffer();
        while (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z') {
            s.append((char)ch);
            get();
        }
        skip();
        return s.toString();
    }
    
    private String readstring() {
        skip();
        StringBuffer s = new StringBuffer();
        if (ch == '\'' || ch == '"') {
            int match = ch;
            
            get();
            while (ch != -1 && ch != match) {
                s.append((char)ch);
                get();
            }
            if (ch == match)
                get();
            skip();
        }
        return s.toString();
    }
    
    private String parseEncoding() {
        getpos = 0;
        get();

        skip();
        if (ch == '<') {
            get();
            get();  // ?
            String tagname = readname();
            if(tagname.equalsIgnoreCase("xml")) {
                while (ch != -1 && (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z')) {
                    String n = readname();
                    if (ch == '=') {
                        get();
                        String val = readstring();
                        if (n.equalsIgnoreCase("encoding") && !val.equals(""))
                            return val;
                    } else
                        break;
                }
            }
        }
        return defaultenc;
    }
    
}

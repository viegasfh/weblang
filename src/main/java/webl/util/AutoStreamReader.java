package webl.util;

import webl.page.*;
import webl.page.net.*;
import java.io.*;
import java.util.*;

public class AutoStreamReader extends Reader {
    private Reader reader;
    private byte                    bb[];		/* Input buffer */
    private String                  defaultenc;
    private String encoding;

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
	    this.defaultenc = defaultenc;
	    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
	    int nRead = -1;

	    // fill the buffer with all the bytes in the InputStream
	    while ((nRead = in.read()) != -1) {
	    	byteBuffer.wrie(nRead);
	    }

	    // now convert the buffer 
	    // to a byte array
	    bb = byteBuffer.toByteArray();

	    if (!enc.equals(""))
	      this.encoding = MIMEType.UnAliasCharset(enc);
	    else
	    	this.encoding = selectEncoder();

	    InputStream is = new ByteArrayInputStream(bb);
	    InputStreamReader isReader = new InputStreamReader(is, this.encoding);
	    reader = new BufferedReader(isReader);
    }

    public String getEncoding() {
	    if (this.encoding != "")
		    return this.encoding;
	    else
		    return null;
    }

    public int read() throws IOException {
    	return reader.read();
    }

    public int read(char cbuf[], int off, int len) throws IOException {
    	return reader.read(cbuf, off, len);
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
    String selectEncoder() {
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

        return enc;
    }

    public boolean ready() throws IOException {
    	return reader.ready();
    }

    public void close() throws IOException {
	    if (reader != null) {
    	    reader.close();
    	    bb = null;
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

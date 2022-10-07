package webl.page.net;

import java.util.*;
import webl.util.*;

public class MIMEType
{
    String      buf;
    int         len;
    int         pos;
    int         ch;

    String      type;
    String      subtype;
    Hashtable   param = new Hashtable();

    public MIMEType(String name) throws IllegalMIMETypeException {
        buf = name;
        len = name.length();
        pos = 0;
        get();
        skip();

        type = readname().toLowerCase();
        if (ch == '/') {
            get();
            subtype = readname().toLowerCase();
            while(ch == ';') {
                get(); skip();
                String attr = readname();
                if (ch == '=') {
                    get(); skip();
                    String val;
                    if (ch == '\'' || ch == '"')
                        val = readstring();
                    else
                        val = readname();
                    param.put(attr.toLowerCase(), val);
                }
                skip();
            }
        }
        if (ch != -1)
            throw new IllegalMIMETypeException("illegal mimetype " + buf);
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public String getTypeSlashSubType() {
        StringBuffer s = new StringBuffer();
        s.append(type).append('/').append(subtype);
        return s.toString();
    }

    public String getParameter(String p) {
        Object o = param.get(p);
        if (o != null)
            return (String)o;
        else
            return null;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();

        s.append(type).append('/').append(subtype);

        Enumeration enumeration = param.keys();
        while (enumeration.hasMoreElements()) {
            String attr = (String)(enumeration.nextElement());
            String val = (String)(param.get(attr));
            s.append(" ;").append(attr).append("=\"").append(val).append('\"');
        }
        return s.toString();
    }

    private void get() {
        try {
            ch = buf.charAt(pos++);
        } catch (StringIndexOutOfBoundsException e) {
            ch = -1;
        }
    }

    private void skip() {
        while (ch != -1 && ch <= ' ')
            get();
    }

    private String readname() {
        StringBuffer s = new StringBuffer();
        while (ch != -1 && ch > ' ' && !tspecial(ch)) {
            s.append((char)ch);
            get();
        }
        skip();
        return s.toString();
    }

    private String readstring() {
        int match = ch;
        get();

        StringBuffer s = new StringBuffer();
        while (ch != -1 && ch != match) {
            s.append((char)ch);
            get();
        }
        if (ch == match)
            get();
        skip();
        return s.toString();
    }

    boolean tspecial(int ch) {
        return ch == '(' || ch == ')' || ch == '<' || ch == '>' || ch == '@' ||
               ch == ',' || ch == ';' || ch == ':' || ch == '\\' || ch == '"' ||
               ch == '/' || ch == '[' || ch == ']' || ch == '?' || ch == '=';
    }

// alias tables for MIME charset parameter

    private static Hashtable aliases = new Hashtable();

    private static void putalias(String alias, String charset) {
        aliases.put(alias.toLowerCase(), charset);
    }

    static {
        putalias("UTF-8", "UTF8");
        putalias("UTF-16", "Unicode");
        putalias("UTF8", "UTF8");
        putalias("UTF16", "Unicode");
        putalias("UNICODE", "Unicode");
        putalias("UNICODE 1-1", "Unicode");
        putalias("UNICODE-1-1", "Unicode");
        putalias("UNICODE-1-1-UTF-8", "UTF8");
        putalias("US-ASCII", "8859_1");
        putalias("LATIN1", "8859_1");
        putalias("8859-1", "8859_1");
        putalias("iso-8859-1", "8859_1");
        putalias("iso8859", "8859_1");
    }

    public static String UnAliasCharset(String enc) {
        Object o = aliases.get(enc.toLowerCase());
        if (o != null)
            return (String)o;
        else
            return enc;
    }
}

/* MIME type syntax according to RFC 2045:

    content := type "/" subtype
                *(";" parameter)
                ; Matching of media type and subtype
                ; is ALWAYS case-insensitive.

     type := discrete-type / composite-type

     discrete-type := "text" / "image" / "audio" / "video" /
                      "application" / extension-token

     composite-type := "message" / "multipart" / extension-token

     extension-token := ietf-token / x-token

     ietf-token := <An extension token defined by a
                    standards-track RFC and registered
                    with IANA.>

     x-token := <The two characters "X-" or "x-" followed, with
                 no intervening white space, by any token>

     subtype := extension-token / iana-token

     iana-token := <A publicly-defined extension token. Tokens
                    of this form must be registered with IANA
                    as specified in RFC 2048.>

     parameter := attribute "=" value

     attribute := token
                  ; Matching of attributes
                  ; is ALWAYS case-insensitive.

     value := token / quoted-string

     token := 1*<any (US-ASCII) CHAR except SPACE, CTLs,
                 or tspecials>

     tspecials :=  "(" / ")" / "<" / ">" / "@" /
                   "," / ";" / ":" / "\" / <">
                   "/" / "[" / "]" / "?" / "="
                   ; Must be in quoted-string,
                   ; to use within parameter values

*/

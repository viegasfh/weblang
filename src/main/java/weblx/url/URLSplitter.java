package weblx.url;

import webl.lang.*;
import webl.lang.expr.*;

public class URLSplitter {

    String url;

    char ch;
    int pos, len;

    public String   scheme = "";
    public String   host = "";
    public int      port = 0;
    public String   path = "";
    public String   query = "";
    public String   ref = "";
    
    public String   user = "";
    public String   password = "";
    public char     ftptype = 0;

    public URLSplitter(String url) throws MalformedURL {
        this.url = url;
        len = url.length();
        pos = 0;
        Get();
        Scan();
    }

    public ObjectExpr toObject() {
        ObjectExpr obj = new ObjectExpr();
        
        if (scheme.equals("http")) {
            obj.def("scheme", Program.Str(scheme));
            obj.def("host", Program.Str(host));
            if (port != 0) 
                obj.def("port", Program.Int(port));
            obj.def("path", Program.Str(path));  
            obj.def("query", Program.Str(query));
            obj.def("ref", Program.Str(ref));
        } else if (scheme.equals("ftp")) {
            obj.def("scheme", Program.Str(scheme));
            obj.def("host", Program.Str(host));
            obj.def("user", Program.Str(user));  
            obj.def("password", Program.Str(password));
            if (port != 0) 
                obj.def("port", Program.Int(port));
            obj.def("path", Program.Str(path));
            obj.def("type", Program.Chr(ftptype));
        } else if (scheme.equals("file")) {
            obj.def("scheme", Program.Str(scheme));
            obj.def("host", Program.Str(host));
            obj.def("path", Program.Str(path));
            obj.def("ref", Program.Str(ref));
        } else if (!scheme.equals("")) {
            obj.def("url", Program.Str(url));
        } else {
            obj.def("scheme", Program.Str(scheme));
            obj.def("host", Program.Str(host));
            obj.def("path", Program.Str(path));  
            obj.def("query", Program.Str(query));
            obj.def("ref", Program.Str(ref));
        }
        return obj;
    }
    
    private void Get() {
        ch = 0;
        while (pos < len) {
            ch = url.charAt(pos++);
            if (ch != '\n' && ch != '\r')       // strip these out
                break;
        }
    }

    private void Scan() throws MalformedURL {
        if (Letter(ch)) {
            StringBuffer s = new StringBuffer();
            while (Letter(ch)) {
                s.append(ch);
                Get();
            }
            if (ch == ':') {
                Get();
                scheme = s.toString();

                if (scheme.equals("http"))
                    ScanHTTP();
                else if (scheme.equals("ftp"))
                    ScanFTP();
                else if (scheme.equals("file"))
                    ScanFile();
                else
                    return;

                if (ch != 0)
                    throw new MalformedURL("illegal character " + ch);
                return;
            } else {    // might be part of a path we scanned so far
                s.append(ScanHSegment());
                ScanHPath();
                path = s.toString() + path;
                query = ScanSearch();
                if (ch == '#') {
                    Get();
                    ref = ScanRef();
                }                
            }
        } else {
            StringBuffer s = new StringBuffer();
            s.append(ScanHSegment());
            ScanHPath();
            path = s.toString() + path;
            query = ScanSearch();
            if (ch == '#') {
                Get();
                ref = ScanRef();
            }              
            if (ch != 0)
                throw new MalformedURL("illegal character " + ch);
        } 
    }

    private void ScanHTTP() throws MalformedURL {
        Match('/');
        Match('/');
        ScanHostPort();
        if (ch == '/') {
            ScanHPath();
            query = ScanSearch();
            if (ch == '#') {
                Get();
                ref = ScanRef();
            }
        }
    }

    private void ScanHostPort() throws MalformedURL {
        ScanHost();
        if (ch == ':') {
            Get();
            ScanPort();
        }
    }

    private void ScanHost() throws MalformedURL {
        StringBuffer s = new StringBuffer();
        while (HostLetter(ch)) {
            s.append(ch);
            Get();
        }
        host = s.toString();
    }

    private void ScanPort() throws MalformedURL {
        port = 0;
        while (Digit(ch)) {
            port = port * 10 + ch - '0';
            Get();
        }
    }

    // don't expand escapes in the query string
    private String ScanSearch() throws MalformedURL {
        if (ch == '?') {
            Get();
            StringBuffer s = new StringBuffer();
            s.append('?');
            while (SearchLetter(ch)) {
                s.append(ch);
                Get();
            }
            return s.toString();
        } else
            return "";
    }
    
    private void ScanHPath() throws MalformedURL {
        StringBuffer s = new StringBuffer();
        while (ch == '/') {
            s.append('/');
            Get();
            s.append(ScanHSegment());
        }
        path = s.toString();
    }

    private String ScanHSegment() throws MalformedURL {
        StringBuffer s = new StringBuffer();
        while (HSegmentLetter(ch)) {
            s.append(ch);
            Get();
        }
        return s.toString();
    }

    private String ScanRef() throws MalformedURL {
        StringBuffer s = new StringBuffer();
        while (ch != 0) {
            s.append((char)ch);
            Get();
        }
        return s.toString();
    }

    private void ScanFTP() throws MalformedURL {
        Match('/');
        Match('/');
        ScanLogin();
        if (ch == '/') {
            ScanFPath();
            if (ch == ';') {
                Get();
                ScanFTPType();
            }
        }
    }

    private void ScanLogin() throws MalformedURL {
        // look ahead for @ character
        int p = pos;
        int c = ch;
        while (c != 0 && c != '/' && c != '@') {
            if (p < len)
                c = url.charAt(p++);
            else
                c = 0;
        }
        if (c == '@') {
            user = ScanUserOrPassword();
            if (ch == ':') {
                Get();
                password = ScanUserOrPassword();
            }
            Match('@');
        }
        ScanHostPort();
    }

    private String ScanUserOrPassword() {
        StringBuffer s = new StringBuffer();
        while (UChar(ch) || ch == ';' || ch == '?' || ch == '&' || ch == '=') {
            s.append((char)ch);
            Get();
        }
        return s.toString();
    }

    private void ScanFPath() throws MalformedURL {
        StringBuffer s = new StringBuffer();
        while (ch == '/') {
            s.append('/');
            Get();
            s.append(ScanFSegment());
        }
        path = s.toString();
    }

    private String ScanFSegment() throws MalformedURL {
        StringBuffer s = new StringBuffer();
        while (FSegmentLetter(ch)) {
            s.append(ch);
            Get();
        }
        return s.toString();
    }

    private void ScanFTPType() throws MalformedURL {
        Match('t');
        Match('y');
        Match('p');
        Match('e');
        Match('=');
        if (ch == 'A' || ch == 'I' || ch == 'D')
            ftptype = ch;
        else if (ch == 'a' || ch == 'i' || ch == 'd')
            ftptype = (char)(ch - 'a' + 'A');
        else
            throw new MalformedURL("transmission type must be A, I, or D");
    }

    private void ScanFile() throws MalformedURL {
        String s = "";
        
        Match('/');
        if (ch == '/') {
            Get();
            ScanHost();
        } else
            s = "/" + ScanFSegment();
        if (ch == '/') {
            ScanFPath();
        }
        path = s + path;
        
        if (ch == '#') {
            Get();
            ref = ScanRef();
        }      
    }

    private void Match(char c) throws MalformedURL {
        if (c == ch)
            Get();
        else
            throw new MalformedURL("expected " + c + " at position " + (pos - 1));
    }

    public static boolean HSegmentLetter(char ch) {
        return UChar(ch) || ch == ';' || ch == ':' || ch == '@' || ch == '&' || 
            ch == '=' || ch == '~' || 
            // extra's that are not in the spec but sometimes occurs
            ch == '^' || ch == '\\';
    }

    public static boolean SearchLetter(char ch) {
        return UChar(ch) || National(ch) || ch == ';' || ch == ':' || ch == '@' || ch == '&' || 
                ch == '=' || ch == '~' || ch == '/';
    }
    
    public static  boolean FSegmentLetter(char ch) {
        return UChar(ch) || ch == '?' || ch == ':' || ch == '@' || ch == '&' || ch == '='
                || ch == '\\';
    }

    public static  boolean UChar(char ch) {
        return Unreserved(ch) || Escape(ch);
    }

    public static boolean National(char ch) {
        return ch == '{' || ch == '}' || ch == '|' || ch == '\\' || ch == '^' || ch == '~' || 
                ch == '[' || ch == ']' || ch == '`';
    }
    
    public static  boolean Escape(char ch) {
        return ch == '%';
    }

    public static  boolean Unreserved(char ch) {
        return Letter(ch) || Digit(ch) || Safe(ch) || Extra(ch);
    }

    public static  boolean Safe(char ch) {
        return ch == '$' || ch == '-' || ch == '_' || ch == '.' || ch == '+';
    }

    public static  boolean Extra(char ch) {
        return ch == '!' || ch == '*' || ch == '\'' || ch == '(' || ch == ')' | ch == ',';
    }

    public static  boolean HostLetter(char ch) {
        return Letter(ch) || Digit(ch) || ch == '-' || ch == '.' || ch == '_';
    }

    public static  boolean Letter(char ch) {
        return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
    }

    public static  boolean Digit(char ch) {
        return ch >= '0' && ch <= '9';
    }
}


/*

; The generic form of a URL is:

genericurl     = scheme ":" schemepart

; Specific predefined schemes are defined here; new schemes
; may be registered with IANA

url            = httpurl | ftpurl | newsurl |
                 nntpurl | telneturl | gopherurl |
                 waisurl | mailtourl | fileurl |
                 prosperourl | otherurl


; new schemes follow the general syntax
otherurl       = genericurl

; the scheme is in lower case; interpreters should use case-ignore
scheme         = 1*[ lowalpha | digit | "+" | "-" | "." ]
schemepart     = *xchar | ip-schemepart

; URL schemeparts for ip based protocols:

ip-schemepart  = "//" login [ "/" urlpath ]

login          = [ user [ ":" password ] "@" ] hostport
hostport       = host [ ":" port ]
host           = hostname | hostnumber
hostname       = *[ domainlabel "." ] toplabel
domainlabel    = alphadigit | alphadigit *[ alphadigit | "-" ] alphadigit
toplabel       = alpha | alpha *[ alphadigit | "-" ] alphadigit
alphadigit     = alpha | digit
hostnumber     = digits "." digits "." digits "." digits
port           = digits
user           = *[ uchar | ";" | "?" | "&" | "=" ]
password       = *[ uchar | ";" | "?" | "&" | "=" ]
urlpath        = *xchar    ; depends on protocol see section 3.1

; The predefined schemes:

; FTP (see also RFC959)

ftpurl         = "ftp://" login [ "/" fpath [ ";type=" ftptype ]]
fpath          = fsegment *[ "/" fsegment ]
fsegment       = *[ uchar | "?" | ":" | "@" | "&" | "=" ]
ftptype        = "A" | "I" | "D" | "a" | "i" | "d"

; FILE

fileurl        = "file://" [ host | "localhost" ] "/" fpath

; HTTP

httpurl        = "http://" hostport [ "/" hpath [ "?" search ]]
hpath          = hsegment *[ "/" hsegment ]
hsegment       = *[ uchar | ";" | ":" | "@" | "&" | "=" ]
search         = *[ uchar | ";" | ":" | "@" | "&" | "=" ]

; GOPHER (see also RFC1436)

gopherurl      = "gopher://" hostport [ / [ gtype [ selector
                 [ "%09" search [ "%09" gopher+_string ] ] ] ] ]
gtype          = xchar
selector       = *xchar
gopher+_string = *xchar

; MAILTO (see also RFC822)

mailtourl      = "mailto:" encoded822addr
encoded822addr = 1*xchar               ; further defined in RFC822

; NEWS (see also RFC1036)

newsurl        = "news:" grouppart
grouppart      = "*" | group | article
group          = alpha *[ alpha | digit | "-" | "." | "+" | "_" ]
article        = 1*[ uchar | ";" | "/" | "?" | ":" | "&" | "=" ] "@" host

; NNTP (see also RFC977)

nntpurl        = "nntp://" hostport "/" group [ "/" digits ]

; TELNET

telneturl      = "telnet://" login [ "/" ]

; WAIS (see also RFC1625)

waisurl        = waisdatabase | waisindex | waisdoc
waisdatabase   = "wais://" hostport "/" database
waisindex      = "wais://" hostport "/" database "?" search
waisdoc        = "wais://" hostport "/" database "/" wtype "/" wpath
database       = *uchar
wtype          = *uchar
wpath          = *uchar

; PROSPERO

prosperourl    = "prospero://" hostport "/" ppath *[ fieldspec ]
ppath          = psegment *[ "/" psegment ]
psegment       = *[ uchar | "?" | ":" | "@" | "&" | "=" ]
fieldspec      = ";" fieldname "=" fieldvalue
fieldname      = *[ uchar | "?" | ":" | "@" | "&" ]
fieldvalue     = *[ uchar | "?" | ":" | "@" | "&" ]

; Miscellaneous definitions

lowalpha       = "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" |
                 "i" | "j" | "k" | "l" | "m" | "n" | "o" | "p" |
                 "q" | "r" | "s" | "t" | "u" | "v" | "w" | "x" |
                 "y" | "z"
hialpha        = "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" |
                 "J" | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" |
                 "S" | "T" | "U" | "V" | "W" | "X" | "Y" | "Z"
alpha          = lowalpha | hialpha
digit          = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" |
                 "8" | "9"
safe           = "$" | "-" | "_" | "." | "+"
extra          = "!" | "*" | "'" | "(" | ")" | ","
national       = "{" | "}" | "|" | "\" | "^" | "~" | "[" | "]" | "`"
punctuation    = "<" | ">" | "#" | "%" | <">

reserved       = ";" | "/" | "?" | ":" | "@" | "&" | "="
hex            = digit | "A" | "B" | "C" | "D" | "E" | "F" |
                 "a" | "b" | "c" | "d" | "e" | "f"
escape         = "%" hex hex


unreserved     = alpha | digit | safe | extra
uchar          = unreserved | escape
xchar          = unreserved | reserved | escape
digits         = 1*digit

*/
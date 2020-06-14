package webl.page.html;

import java.io.*;
import java.util.*;

import webl.dtd.*;
import webl.page.*;
import webl.page.net.*;
import webl.lang.*;
import webl.lang.expr.*;
import webl.util.*;

/*
Comments:

 - inclusions ands exclusions further up the element stack are not handled correctly
 - check character set 
 - URL resolution not completed
 
*/
public final class HTMLParser implements ParserInterface
{
    private BufReader   R;
    private Page        page;
    private EStack      stack = new EStack();
    private DTD         dtd;
    
    String documenturl;
    String documentbase;    
    String dtdoveride;
    
    boolean     resolve = true;
    boolean     emptyparagraph = false;
    boolean     fixhtml = false;
    boolean     expandentities = false;
    
    public HTMLParser() {
    }
    
    static private Counter chtml = new Counter("HTML parsing");

    private final int BUFSIZE = 512;
    private char[] charbuf = new char[BUFSIZE];
    
    public String DefaultCharset() {
        return "ISO-8859-1";
    }
    
    public Page Parse(Reader TR, String documenturl, ObjectExpr options) throws IOException {
        resolve = GetResolveFlag(options);
        emptyparagraph = GetEmptyParagraphFlag(options);
        fixhtml = GetFixHTMLFlag(options);
        expandentities = GetExpandEntitiesFlag(options);
        
        this.dtdoveride = GetStringVal(options, "dtd");
        if(dtdoveride != null && !dtdoveride.equals("")) {
            try {
                dtd = Catalog.OpenDTD(dtdoveride);
                Log.debugln("[Using DTD override " + dtdoveride + "]");
            } catch (FileNotFoundException e) {
                throw new IOException("Unable to locate DTD " + dtdoveride);
            }
        } else {
            try {
                dtd = Catalog.OpenHTML32();
            } catch (FileNotFoundException e) {
                throw new InternalError("Panic: Unable to locate the HTML3.2 DTD");
            }
        }
        this.documentbase = documenturl;
        this.documenturl = documenturl;
        this.R = new BufReader(TR);
        page = new Page(dtd, Page.HTML);
        
        chtml.begin();
        while (R.ch != -1) {
            switch (R.ch) {
                case '<':                               // start of some markup
                    R.adv();
                    if (NameStartChar(R.ch) || R.ch == '/')
                        scanTag();
                    else if (R.ch == '!')
                        scanBang();
                    else {
                        ProcessError(R.getLine(), "illegal character following <");
                        ProcessPCData("<");
                    }                
                    break;
                default:
                    StringBuffer s = new StringBuffer();
                    int pos = 0;
                    while(R.ch != -1 && R.ch != '<') {
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
                    ProcessPCData(s.toString()); 
                // note: character references are not handled
            }
        }
        ProcessFin();
        TR.close();
        chtml.end();
        return page;
    }
    
    private void scanTag() throws IOException {
        // assert(R.ch == '/' || NameStartChar(R.ch));
        if (R.ch == '/') {
            R.adv();
            ProcessCloseTag(scanName());
            expect('>');
        } else {
            String elemname = scanName();
            DTDElement E = dtd.getElement(elemname);
            Piece p = ProcessOpenTag(elemname, E);
            while (NameStartChar(R.ch)) {                 // parse the attributes
                String name = scanName();
                if (R.ch == '=') {
                    R.get();
                    String val;
                    if (R.ch == '"' || R.ch == '\'')
                        val = scanLit();
                    else
                        val = scanVal();
                    ProcessTagAttr(p, E, name, val);
                } else {
                    ProcessTagAttr(p, E, name, "");
                }
                
                // skip over possible errors
                if (R.ch != -1 && !NameStartChar(R.ch) && R.ch != '>') {
                    ProcessError(R.getLine(), "skipping over illegal attribute starting with " + (char)(R.ch));
                    while (R.ch != -1 && !NameStartChar(R.ch) && R.ch != '>')
                        R.get();
                }
                
            }
            expect('>');
            
            // Special case for handling JavaScript/VBScript code, which may contain spurious tags
            // that might throw off the parser.
            if (elemname.equals("script")) {
                String scriptcode = R.skipIgnoreCaseTill("</script");
                R.skip();       // paranoid: there might be spaces between "</script" and ">"
                expect('>');
                
                ProcessPCData(scriptcode);
                ProcessCloseTag(elemname);
            }
        }
    }
    
    // scanning of markup of the form <! only in the document (not in the internal or external subset)
    private void scanBang() throws IOException {
        // assert(R.ch == '!');
        int linestart = R.getLine();
        R.adv();
        
        if (R.ch == '-') {                            // comment start
            R.adv();                                  // eat -, seen <!- so far
            if (R.ch == '-') {
                R.adv();                              // eat -, seen <!-- so far
                ProcessComment(R.skipTill("-->"));
            } else {
                ProcessError(linestart, "unexpected symbol after <!-");
                ProcessPCData("<!-");
            }             
        } else if (NameStartChar(R.ch)) {
            String typ = scanName();
            if (typ.equalsIgnoreCase("DOCTYPE"))
                scanDocType();
            else {
                ProcessError(linestart, "<!DOCTYPE expected");
                R.skipTill(">");
            }
        } else
            ProcessError(R.getLine(), "unexpected character following <!");
    }

    private String scanName() throws IOException {
        if (!NameStartChar(R.ch)) {
            ProcessError(R.getLine(), "name character expected"); return "";
        }
        StringBuffer s = new StringBuffer();
        int pos = 0;
        while(NameChar(R.ch)) {
            if (pos < BUFSIZE) {
                charbuf[pos++] = Character.toLowerCase((char)R.ch);
                R.adv();
            } else {
                s.append(charbuf, 0, pos);
                pos = 0;
            }
        }
        if (pos > 0)
            s.append(charbuf, 0, pos);
        // skip trailing white space
        R.skip();
        return s.toString();      // HTML names always converted to lowercase
    }       
    
    private String scanLit() throws IOException {
        if (R.ch == '"' || R.ch == '\'') {
            int fin = R.ch;
            int linestart = R.getLine();
            R.adv();
            
            int pos = 0;
            StringBuffer s = new StringBuffer();
            while (Char(R.ch) && R.ch != fin) {
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
            expect(linestart, fin);
            R.skip();    
            return s.toString();            
        } else {
            ProcessError(R.getLine(), "\" or \' expected");
            return "";
        }
    }
    
    private String scanVal() throws IOException {
        StringBuffer s = new StringBuffer();
        int pos = 0;
        while (Char(R.ch) && R.ch > ' ' && R.ch != '>') {
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
        R.skip();    
        return s.toString();            
    }
    
    private void scanDocType() throws IOException {
        R.startRecording();
        String typ = scanName();  
        String id = scanName();
        String dtd = scanLit();
        String url = "";
        if (R.ch == '"' || R.ch == '\'') url = scanLit();
        
        ProcessDocType(R.stopRecording(), typ, id, dtd, url);
        expect('>');
    }

    static private String GetStringVal(ObjectExpr obj, String fld) {
        if (obj == null)
            return null;
            
        Expr r = obj.get(fld);
        if (r != null && r instanceof StringExpr)
            return ((StringExpr)r).val();
        return null;
    }
    
    static private boolean GetResolveFlag(ObjectExpr obj) {
        if (obj == null)
            return true;
            
        Expr r = obj.get("resolveurls");
        if (r == Program.falseval)
            return false;
        else
            return true;
    }
    
    static private boolean GetEmptyParagraphFlag(ObjectExpr obj) {
        if (obj == null)
            return false;
            
        Expr r = obj.get("emptyparagraphs");
        if (r == Program.trueval)
            return true;
        else
            return false;
    }
    
    static private boolean GetFixHTMLFlag(ObjectExpr obj) {
        if (obj == null)
            return false;
            
        Expr r = obj.get("fixhtml");
        if (r == Program.trueval)
            return true;
        else
            return false;
    }
    
    static private boolean GetExpandEntitiesFlag(ObjectExpr obj) {
        if (obj == null)
            return false;
            
        Expr r = obj.get("expandentities");
        if (r == Program.trueval)
            return true;
        else
            return false;
    }
    
    private void expect(int line, int sym) throws IOException {
        if (R.ch == sym)
            R.adv();
        else
            ProcessError(line, (char)sym + " expected");
    }
    
    private void expect(int sym) throws IOException {
        expect(R.getLine(), sym);
    }    
    
    private void expect(String s) throws IOException {
        for(int i = 0; i < s.length(); i++)
            expect(R.getLine(), s.charAt(i));
    }
    
    final boolean Char(int ch) {
        return ch != -1;
/*        
        return ch == 0x9 || ch == 0xA || ch == 0xD ||
            (ch >= 0x20 && ch <= 0xFFFD) ||
            (ch >= 0x1000 && ch <= 0x7FFFFFFF);
*/            
    }    
    
    final boolean NameChar(int ch) {
        return ch >= 'a' && ch <= 'z'
            || ch >= 'A' && ch <= 'Z'
            || ch >= '0' && ch <= '9'
            || ch == '.'
            || ch == '-' 
            || ch == '_' 
            || ch == ':';
    }    
    
    final boolean NameStartChar(int ch) {
        return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch == '_' || ch == ':';
    }        
    
// actual page construction happens here

    void ProcessError(int lineno, String s) {
        Log.debugln("[ line " + lineno + ": " + s + " (" + documenturl + ")]");
    }
    
    void ProcessPCData(String s) {
        if (expandentities)
            s = dtd.ExpandCharEntities(s);
        page.appendPCData(s);
    }
    
    boolean validChild(ElemEntry e, String name) {
        DTDElement E = e.dtdelem;
        return (E == null) || E.validChild(name);
    }
    
    void ensurePreconditions(String name) {
        if (!stack.empty()) {
            ElemEntry e = stack.top;
            
            while (e != null && !validChild(e, name)) {
                if (!fixhtml && e.dtdelem != null && !e.dtdelem.optionalEndTag())
                    break;
                e = e.next;
            }
            if (e != null && validChild(e, name)) {
                while (stack.top != e) {
                    page.appendCloseTag(stack.top.p);
                    stack.pop();
                }
            } else if (e != null) {
                ProcessError(R.getLine(), "invalid parent element for " + name + ": " + e.name);
            }
        }
    }

    final private boolean EmptyParagraph(String name) {
        return name.equals("p") && emptyparagraph;
    }
    
    Piece ProcessOpenTag(String name, DTDElement E) {
        ensurePreconditions(name);
        
        Piece p = page.appendOpenTag(name);
        if (E != null) {
            if (E.EmptyElement() || EmptyParagraph(name)) 
                page.makeEmptyTag(p);
            else 
                stack.push(name, E, p);
        } else {
            stack.push(name, null, p);
            ProcessError(R.getLine(), "unknown element " + name);
        }
        return p;
    }
    
    
    void ProcessCloseTag(String name) {
        // first check if we have an opening tag on the stack
        ElemEntry e = stack.top;
        while (e != null && !e.name.equals(name)) e = e.next;
        
        if (e == null) {
            ProcessError(R.getLine(), "no matching begin tag for " + name);
            return;
        }
        
        // close opening tags until a matching tag is found
        e = stack.top;
        while (e != null) {
            if(e.name.equals(name)) {
                page.appendCloseTag(e.p);
                stack.remove(e);
                return;
            } else {
                // ProcessError(R.getLine(), "inserting tag /" + e.name + " because of /" + name);
                page.appendCloseTag(e.p);
                stack.pop();
            }
            e = stack.top;
        }
        throw new InternalError("unreachable code");
    }
    
    void ProcessTagAttr(Piece p, DTDElement E, String name, String value) {
        if (p.name.equalsIgnoreCase("BASE") && name.equalsIgnoreCase("href")) {  // base tag
            documentbase = Net.ResolveBASE(documenturl, value);
        } else if (E != null) {
            DTDAttribute A = E.getAttribute(name);
            
            // check if there is an URL to resolve
            if (resolve && A != null && A.type instanceof String && ((String)A.type).equalsIgnoreCase("url")) {
                value = Net.ResolveHREF(documentbase, value);
            }
        }
        p.setAttr(name, value);
    }
    
    void ProcessDocType(String val, String typ, String id, String dtdname, String url) {
        page.appendDoctype(val);
        
        // load the appropriate dtd
        try {
            if (dtdoveride != null && !dtdoveride.equals("")) {
                Log.debugln("[Ignoring specified DTD " + dtdname + ", using override instead]");
            } else {
                Log.debugln("[Using HTML DTD named " + dtdname + " to parse page]");
                dtd = Catalog.OpenDTD(dtdname);
            }
            page.dtd = dtd;                 // write over previous value
        } catch (IOException e) {
            Log.debugln("[Unable to load " + dtdname + "]");
        }
    }
    
    void ProcessComment(String s) {
        page.appendComment(s);
    }
    
    void ProcessFin() {
        while (!stack.empty()) {
            ProcessError(R.getLine(), "closing unmatched tag at end of page" + stack.top.name);
            page.appendCloseTag(stack.top.p);
            stack.pop();
        }
    }
}


class EStack
{
    ElemEntry top;
    
    boolean empty() {
        return top == null;
    }
    
    void push(String name, DTDElement dtdelem, Piece p) {
        ElemEntry e = new ElemEntry(name, dtdelem, p);
        e.next = top;
        top = e;
    }
    
    void pop() {
        if (top != null) top = top.next;
    }
    
    void remove(ElemEntry e) {
        if (e == top)
            top = top.next;
        else {
            ElemEntry x = top;
            ElemEntry p = null;
            while (x != null && x != e) { p = x; x = x.next; }
            if (x != null)
                p.next = x.next;
        }
    }
}

class ElemEntry 
{
    ElemEntry next;             // next entry on the stack
    
    String name;
    DTDElement dtdelem;
    Piece p;
    
    public ElemEntry(String name, DTDElement dtdelem, Piece p) {
        this.name = name;
        this.dtdelem = dtdelem;
        this.p = p;
    }
}

package webl.page.xml;

import java.io.*;
import java.util.*;
import webl.page.*;
import webl.page.net.*;
import webl.lang.expr.*;

import webl.util.*;

public class XMLParser implements ParserInterface
{
    private BufReader   R;
    private Page        page;
    private Vector<Piece>      stck = new Vector<Piece>();
    
    private String      documenturl;
    
    public XMLParser() {
    }

    public String DefaultCharset() {
        return "UTF8";
    }
    
    public Page Parse(Reader TR, String url, ObjectExpr options) throws IOException {
        documenturl = url;
        page = new Page(null, Page.XML);
        this.R = new BufReader(TR);
        
        while (R.ch != -1) {
            switch (R.ch) {
                case '<':                               // start of some markup
                    R.adv();
                    if (NameStartChar(R.ch) || R.ch == '/')
                        scanTag();
                    else if (R.ch == '?') {
                        R.adv();
                        ProcessPI(scanName(), R.skipTill("?>"));
                    } else if (R.ch == '!')
                        scanBang();
                    else {
                        ProcessError(R.getLine(), "illegal character following <");
                        ProcessPCData("<");
                    }                
                    break;
                default:
                    StringBuffer s = new StringBuffer();
                    while(R.ch != -1 && R.ch != '<') {
                        s.append((char)R.ch);
                        R.adv();
                    }
                    ProcessPCData(s.toString()); 
                // note: character references are not handled
            }
        }
        ProcessFin();
        TR.close();
        return page;
    }
    
    private void scanTag() throws IOException {
        // assert(R.ch == '/' || NameStartChar(R.ch));
        if (R.ch == '/') {
            R.adv();
            ProcessCloseTag(scanName());
        } else {
            Piece p = ProcessOpenTag(scanName());
            while (NameStartChar(R.ch)) {                 // parse the attributes
                String name = scanName();
                if (R.ch == '=') {
                    R.get();
                    String val = scanLit();
                    ProcessTagAttr(p, name, val);
                } else
                    ProcessError(R.getLine(), "= expected");
            }
            if (R.ch == '/') {
                R.adv();
                ProcessEmptyTag(p);
            }
        }
        expect('>');
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
        } else if (R.ch == '[') {                    // only CDATA is allowed here
            R.adv();
            expect("CDATA[");
            ProcessCData(R.skipTill("]]>"));
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
        while(NameChar(R.ch)) {
            s.append((char)R.ch);
            R.adv();
        }
        // skip trailing white space
        R.skip();
        return s.toString();        // note: XML is case sensitive
    }       
    
    private String scanLit() throws IOException {
        if (R.ch == '"' || R.ch == '\'') {
            int fin = R.ch;
            int linestart = R.getLine();
            R.adv();
            
            StringBuffer s = new StringBuffer();
            while (Char(R.ch) && R.ch != fin) {
                s.append((char)R.ch);
                R.adv();
            }
            expect(linestart, fin);
            R.skip();    
            return s.toString();            
        } else {
            ProcessError(R.getLine(), "\" or \' expected");
            return "";
        }
    }
    
    // just skip over the whole doctype tag
    private void scanDocType() throws IOException {
        // skip till close of the [ that starts the markup declarations of the
        // internal DTD subset
        R.startRecording();
        while(R.ch != -1 && R.ch != '>' && R.ch != '[') {
            if (R.ch == '"' || R.ch == '\'') 
                skiplit();
            else
                R.adv();
        }
        
        // eat markup declarations
        if (R.ch == '[') {                              // scan markup declarations
            R.adv();
            while(R.ch != -1 && R.ch != ']') {
                if (R.ch == '"' || R.ch == '\'')        // skip literals
                    skiplit();
                else if (R.ch == '<') {                   // some markup
                    R.adv();
                    if (R.ch == '?') {                  // processing instruction
                        R.adv();
                        R.skipTill("?>");
                    } else if (R.ch == '!')             // comment of DTD declarations
                        skipBang();
                } else
                    R.adv();
            }
            R.adv();                                    // eat ]
            R.skip();
        }
        ProcessDocType(R.stopRecording());
        if (R.ch == '>')
            R.adv();
    }
    
    
    private void skipBang() throws IOException {
        // assert(R.ch == '!');
        R.adv();
        
        if (R.ch == '-') {                            // comment start
            R.adv();                                  // eat -, seen <!- so far
            if (R.ch == '-') {
                R.adv();                              // eat -, seen <!-- so far
                R.skipTill("-->");
            }
        } else if (R.ch == '[') {                     // CDATA, INCLUDE, IGNORE
            R.adv();
            R.skipTill("]]>");
        } else if (NameStartChar(R.ch)) {
            while(R.ch != -1 && R.ch != '>') {
                if (R.ch == '"' || R.ch == '\'') 
                    skiplit();
                else
                    R.adv();
            }            
        }
    }
    
    private void skiplit() throws IOException {
        int fin = R.ch;
        R.adv();
        while (R.ch != -1 && R.ch != fin) R.adv();
        R.adv();
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
        return ch == 0x9 || ch == 0xA || ch == 0xD ||
            (ch >= 0x20 && ch <= 0xFFFD) ||
            (ch >= 0x1000 && ch <= 0x7FFFFFFF);
    }    
    
    final boolean NameChar(int ch) {
        return XMLCharset.NameChar(ch);
    }    
    
    final boolean NameStartChar(int ch) {
        return XMLCharset.NameStartChar(ch);
    }        
    
// actual page construction happens here

    void ProcessError(int lineno, String s) {
        Log.debugln("[ line " + lineno + ": " + s + " (" + documenturl + ")]");
    }
    
    void ProcessPCData(String s) {
        page.appendPCData(s);
    }
    
    Piece ProcessOpenTag(String name) {
        Piece p = page.appendOpenTag(name);
        stck.addElement(p);
        return p;
    }
    
    void ProcessEmptyTag(Piece p) {
        page.makeEmptyTag(p);
        stck.removeElement(p);
    }
    
    void ProcessCloseTag(String name) {
        for(int i = stck.size()-1; i >= 0; i--) {
            Piece p = stck.elementAt(i);
            if(p.name.equals(name)) {
                page.appendCloseTag(p);
                stck.removeElement(p);
                return;
            }
        }
        ProcessError(R.getLine(), "no matching begin tag for " + name);
    }
    
    void ProcessTagAttr(Piece p, String name, String value) {
        p.setAttr(name, value);
    }
    
    void ProcessDocType(String val) {
        page.appendDoctype(val);
    }
    
    void ProcessPI(String target, String val) {
        page.appendPI(target, val);
    }
    
    void ProcessComment(String s) {
        page.appendComment(s);
    }
    
    void ProcessCData(String s) {
        page.appendCData(s);
    }
    
    void ProcessFin() {
        for(int i = stck.size()-1; i >= 0; i--) {
            Piece p = stck.elementAt(i);
            ProcessError(R.getLine(), "closing tag " + p.name);
            page.appendCloseTag(p);
        }
    }
}

package webl.page.xml;

import webl.util.*;
import java.io.*;

//
// class to parse the contents of the XML DOCTYPE tag
//

public class DocTypeParser
{
    BufReader R;
    
    public DocTypeParser(String s) throws IOException {
        this.R = new BufReader(new StringReader(s));
    }
    
   private void Parse() throws IOException {
        String dtdname = scanName();
        if (NameStartChar(R.ch)) {
            String id = scanName();
            if (id.equalsIgnoreCase("SYSTEM")) {
                String lit = scanLit();
            } else if (id.equalsIgnoreCase("PUBLIC")) {
                String lit1 = scanLit();
                String lit2 = scanLit();
            } else
                ProcessError("PUBLIC or SYSTEM expected");
        }
        if (R.ch == '[') {
            R.get();
            scanMarkupDecl();
            expect(']');
        }
        R.skip();
        expect('>');        
    }

    private void scanMarkupDecl() throws IOException {
        R.skip();
        while (R.ch == '<') {
            R.adv();
            if (R.ch == '?') {
                R.adv();
                String target = scanName();
                R.skipTill("?>");            
            } else if (R.ch == '!')
                scanBang();
            else 
                ProcessError("markup declarations for DTD expected");
            R.skip();
        }
    }
    
    private void scanBang() throws IOException {
        // assert(R.ch == '!');
        R.adv();
        if (R.ch == '-') {                            // comment start
            R.adv();                                  // eat -, seen <!- so far
            if (R.ch == '-') {
                R.adv();                              // eat -, seen <!-- so far
                R.skipTill("-->");
            } else 
                ProcessError("unexpected symbol after <!-");
        } else if (R.ch == '[') {                    // only CDATA is allowed here
            scanBrackets();
        } else if (NameStartChar(R.ch)) {
            String typ = scanName();
            if (typ.equalsIgnoreCase("ELEMENT"))
                scanElement();
            else if (typ.equalsIgnoreCase("ATTLIST"))
                scanAttList();
            else if (typ.equalsIgnoreCase("ENTITY"))
                scanEntity();
            else if (typ.equalsIgnoreCase("NOTATION"))
                scanNotation();
            else {
                ProcessError("unknown markup: " + typ);
                R.skipTill(">");                // should do a clever skip here
            }            
        } else
            ProcessError("unexpected character following <!");
    }
        
    private void scanBrackets() throws IOException {
        // assert(R.ch == '[');
        R.get();
        String typ = scanName();
        expect('[');
        
        if (typ.equalsIgnoreCase("INCLUDE")) {
            R.skip();
            scanMarkupDecl();
            expect("]]>");
        } else if (typ.equalsIgnoreCase("IGNORE")) {
            R.skip();
            scanMarkupDecl();
            expect("]]>");
        } else if (typ.equalsIgnoreCase("CDATA")) {
            R.skipTill("]]>");
        } else {
            ProcessError("unknown token following <![");
            R.skipTill("}}>");
        }
    }
    
    private void scanElement() throws IOException {
        String name = scanName();
        if (NameStartChar(R.ch)) {
            String c = scanName();
            if (c.equalsIgnoreCase("ANY")) {
            } else if (c.equalsIgnoreCase("EMPTY")) {
            } else
                scanContent();
        } else
            scanContent();
            
        expect('>');
    }
    
    private void scanContent() throws IOException {
        if (NameStartChar(R.ch)) {
            String c = scanName();
        } else if (R.ch == '#') {
            R.adv();
            String pc = scanName();
        } else if (R.ch == '(') {
            R.get();
            String v = scanName();
            if (R.ch == '|') {
                while (R.ch == '|') {
                    R.get();
                    scanContent();
                }
            } else if (R.ch == ',') {
                while (R.ch == ',') {
                    R.get();
                    scanContent();
                }
            } 
            expect(')');
            R.skip();
            
            if (R.ch == '?') {
                R.get();
            } else if (R.ch == '*') {
                R.get();
            } else if (R.ch == '?') {
                R.get();
            }
        }
    }
    
    private void scanAttList() throws IOException {
        String name = scanName();
        while (NameStartChar(R.ch)) {
            String attrname = scanName();
            
            if (R.ch == '(') {
                R.get();
                String v = scanNmToken();
                while (R.ch == '|') {
                    R.get();
                    v = scanNmToken();
                }
                expect(')');
                R.skip();
            } else {
                String attrtyp = scanName();
                if (attrtyp.equalsIgnoreCase("NOTATION")) {
                    expect('(');
                    R.skip();
                    String v = scanName();
                    while (R.ch == '|') {
                        R.get();
                        v = scanName();
                    }
                    expect(')');
                    R.skip();
                }
            }
            
            if (R.ch == '#') {
                R.adv();
                String attrdefault = scanName();
                if (attrdefault.equalsIgnoreCase("FIXED")) {
                    String val = scanLit();
                }
            } else if (R.ch == '"' || R.ch == '\'') {
                String val = scanLit();
            } else
                ProcessError("# or quote expected");
        }
        
        expect('>');
    }
    
    private void scanEntity() throws IOException {
        if (R.ch == '%')
            R.get();
            
        String name = scanName();
        if (R.ch == '"' || R.ch == '\'')
            scanLit();
        else {
            String id = scanName();
            if (id.equalsIgnoreCase("SYSTEM")) {
                String lit = scanLit();
            } else if (id.equalsIgnoreCase("PUBLIC")) {
                String lit1 = scanLit();
                String lit2 = scanLit();
            } else
                ProcessError("PUBLIC or SYSTEM expected");
            
            if (NameStartChar(R.ch)) {
                String n = scanName();
                if (n.equalsIgnoreCase("NDATA")) {
                    String val = scanName();
                } else
                    ProcessError("NDATA expected");
            }
        }
        expect('>');
    }
    
    private void scanNotation() throws IOException {
        String name = scanName();
        String id = scanName();
        if (id.equalsIgnoreCase("SYSTEM")) {
            String lit = scanLit();
        } else if (id.equalsIgnoreCase("PUBLIC")) {
            String lit1 = scanLit();
            String lit2 = scanLit();
        } else
            ProcessError("PUBLIC or SYSTEM expected");
        expect('>');
    }
    
    private String scanName() throws IOException {
        if (!NameStartChar(R.ch)) {
            ProcessError("name character expected"); return "";
        }
        StringBuffer s = new StringBuffer();
        while(NameChar(R.ch)) {
            s.append((char)R.ch);
            R.adv();
        }
        // skip trailing white space
        R.skip();
        return s.toString();
    }       
    
    private String scanNmToken() throws IOException {
        if (!NameChar(R.ch)) {
            ProcessError("name token character expected"); return "";
        }
        StringBuffer s = new StringBuffer();
        while(NameChar(R.ch)) {
            s.append((char)R.ch);
            R.adv();
        }
        // skip trailing white space
        R.skip();
        return s.toString();
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
            ProcessError("\" or \' expected");
            return "";
        }
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
    
    boolean Char(int ch) {
        return ch == 0x9 || ch == 0xA || ch == 0xD ||
            (ch >= 0x20 && ch <= 0xFFFD) ||
            (ch >= 0x1000 && ch <= 0x7FFFFFFF);
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
    
    boolean NameStartChar(int ch) {
        return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch == '_' || ch == ':';
    }        
    
    void ProcessError(int line, String msg) {
    }
    
    void ProcessError(String msg) {
        ProcessError(R.getLine(), msg);
    }
}

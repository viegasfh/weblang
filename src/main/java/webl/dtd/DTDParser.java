package webl.dtd;

import java.io.*;
import java.util.*;

import webl.util.*;

public class DTDParser
{
    private MacroBufReader  R;
    private DTD dtd = new DTD();

    public DTDParser(Reader R) throws IOException {
        this.R = new MacroBufReader(R, dtd.getMacroEntities());
    }

    static private Counter cdtd = new Counter("DTD parsing");

    public DTD Parse() throws IOException {
        cdtd.begin();
        R.skip();
        while (R.ch == '<') {
            scanMarkup();
        }
        if (R.ch != -1)
            ProcessError(R.getLine(), "unexpected symbol");
        cdtd.end();
        return dtd;
    }

    private void scanMarkup() throws IOException {
        expect("<!");
        int linestart = R.getLine();
        if (R.ch == '-') {                            // comment start
            R.expanding(false);
            R.adv();                                  // eat -, seen <!- so far
            if (R.ch == '-') {
                R.adv();                              // eat -, seen <!-- so far
                R.skipTill("-->");
            } else {
                ProcessError(linestart, "unexpected symbol after <!-");
            }
            R.expanding(true);
        } else if (R.ch == '[') {
            R.get();
            String s = scanName();
            if (s.equalsIgnoreCase("INCLUDE")) {
                expect('[');
                R.skip();
                while (R.ch == '<') scanMarkup();
            } else if (s.equalsIgnoreCase("IGNORE")) {
                R.expanding(false);
                expect('[');
                // skip over everything
                while (true) {
                    if (R.ch == -1 || R.ch == ']') break;
                    else if (R.ch == '"' || R.ch == '\'') scanLit();
                    else
                        R.adv();
                }
                R.expanding(true);
            } else
                ProcessError(linestart, "expected IGNORE or INCLUDE");
            expect("]]>");
            R.skip();
        } else if (NameStartChar(R.ch)) {
            String typ = scanName();
            if (typ.equalsIgnoreCase("ENTITY"))
                scanEntity();
            else if (typ.equalsIgnoreCase("ELEMENT"))
                scanElement();
            else if (typ.equalsIgnoreCase("ATTLIST"))
                scanAttList();
            else {
                ProcessError(linestart, "valid tag expected");
                R.skipTill(">");
            }
        } else
            ProcessError(R.getLine(), "unexpected character following <!");
        R.skip();
    }

    private void scanEntity() throws IOException {
        boolean charentity = true;

        if (R.ch == '%') {
            R.get();
            charentity = false;
        }

        String entityname = scanName();

        if (NameStartChar(R.ch)) {
            String loc = scanName();        // either PUBLIC or SYSTEM
            String s = scanLit();

            if (loc.equalsIgnoreCase("PUBLIC") || s.equalsIgnoreCase("SYSTEM"))
                try {
                    String entityval = Catalog.importDTD(s);
                    dtd.addMacroEntity(entityname, entityval);
                } catch (FileNotFoundException e) {
                    ProcessError(R.getLine(), "could not locate DTD named " + s);
                }
            else if (loc.equalsIgnoreCase("CDATA")) {
                if (charentity)
                    dtd.addCharEntity(entityname, s);
                else
                    dtd.addMacroEntity(entityname, s);
            }
        } else {
            String entityval = scanLit();
            if (charentity)
                dtd.addCharEntity(entityname, entityval);
            else
                dtd.addMacroEntity(entityname, entityval);
        }
        Comment();
        expect(">");
    }

    private void scanElement() throws IOException {
        boolean optopen = false, optclose = false;
        webl.util.Set exclusions = null, inclusions = null;

        webl.util.Set S = scanNames();

        // opening tag
        if (R.ch == '-') {
            R.get();
        } else if (R.ch == 'O' || R.ch == 'o') {
            R.get();
            optopen = true;
        } else
            ProcessError(R.getLine(), "- or O expected");

        // closing tag
        if (R.ch == '-') {
            R.get();
        } else if (R.ch == 'O' || R.ch == 'o') {
            R.get();
            optclose = true;
        } else
            ProcessError(R.getLine(), "- or O expected");

        webl.util.Set children = new webl.util.Set();
        scanContentModel(children);

        // exclusions
        if (R.ch == '-') {
            R.get();
            if (R.ch == '-') {      // small hack to handle distinction between comments & exclusions
                R.adv();
                R.skipTill("--");
            } else
                exclusions = scanNames();
        }

        // inclusions
        if (R.ch == '+') {
            R.get();
            inclusions = scanNames();
        }

        Comment();
        expect('>');

        Enumeration enumeration = S.elements();
        while (enumeration.hasMoreElements()) {
            String n = (String)enumeration.nextElement();
            DTDElement E = new DTDElement(n, optopen, optclose, children, exclusions, inclusions);
            dtd.addElement(E);
        }

    }

    private void scanContentModel(webl.util.Set children) throws IOException {
        if (NameStartChar(R.ch)) {
            String name = scanName().toLowerCase();
            if (name.equals("empty")) return;
            children.put(name);

            if (R.ch == '?') R.get();
            else if (R.ch == '*') R.get();
            else if (R.ch == '+') R.get();
            R.skip();
        } else
            scanContentModel0(children);
    }

    // This implementation simply detects which elements may occur in the element this content
    // model belongs too.
    private void scanContentModel0(webl.util.Set children) throws IOException {
        if (R.ch == '(') {
            R.get();
            scanContentModel0(children);
            if (R.ch == ',') {
                while (R.ch == ',') {
                    R.get();
                    scanContentModel0(children);
                }
            } else if (R.ch == '|') {
                while (R.ch == '|') {
                    R.get();
                    scanContentModel0(children);
                }
            } else if (R.ch == '&') {
                while (R.ch == '&') {
                    R.get();
                    scanContentModel0(children);
                }
            } else if (R.ch != ')')
                ProcessError(R.getLine(), "content model error");
            expect(')');
            // R.skip();     don't do this as occurrence indicator has to follow directly after
        } else if (NameStartChar(R.ch)) {
            String name = scanName().toLowerCase();
            children.put(name);
        } else if (R.ch == '#') {       // saw #PCDATA (most probably)
            R.adv();
            String name = scanName().toLowerCase();
            children.put("#" + name);
        } else
            ProcessError(R.getLine(), "content model error");

        if (R.ch == '?') R.get();
        else if (R.ch == '*') R.get();
        else if (R.ch == '+') R.get();

        R.skip();
    }

    private void scanAttList() throws IOException {
        webl.util.Set S = scanNames();

        Comment();
        while(NameStartChar(R.ch)) {
            String attname = scanName().toLowerCase();            // attribute name
            // scan type
            Object typ;
            if (R.ch == '(') {
                webl.util.Set T = new webl.util.Set();
                R.get();
                T.put(scanNameOrNumber().toLowerCase());
                while (R.ch == '|') {
                    R.get();
                    T.put(scanNameOrNumber().toLowerCase());
                }
                expect(')');
                R.skip();
                typ = T;
            } else {
                typ = scanName().toLowerCase();
            }

            // scan default value
            String def = "";
            boolean fixed = false;

            if (R.ch == '#') {
                R.adv();
                def = "#" + scanName().toLowerCase();
                if (def.equals("#fixed")) {
                    fixed = true;
                    if (R.ch == '"' || R.ch == '\'')
                        def = scanLit();
                    else
                        def = scanName().toLowerCase();
                }
            } else if (NameChar(R.ch)) {
                def = scanNameOrNumber().toLowerCase();
            } else if (R.ch == '"' || R.ch == '\'') {
                def = scanLit();
            } else
                ProcessError(R.getLine(), "default value expected");

            Comment();

            Enumeration enumeration = S.elements();
            while (enumeration.hasMoreElements()) {
                String n = (String)enumeration.nextElement();
                DTDAttribute A = new DTDAttribute(attname, typ, def, fixed);
                DTDElement elem = dtd.getElement(n);
                if (elem != null)
                    elem.addAttribute(A);
                else
                    ProcessError(R.getLine(), "no element " + n);
            }

        }
        expect('>');
    }

    private void Comment() throws IOException {
        while (R.ch == '-') {
            R.adv();
            if (R.ch == '-') {
                R.adv();
                R.expanding(false);
                R.skipTill("--");
                R.expanding(true);
                R.skip();
            } else
                ProcessError(R.getLine(), "comment expected after -");
        }
    }


    private final int BUFSIZE = 512;
    private char[] charbuf = new char[BUFSIZE];

    private String scanName() throws IOException {
        if (!NameStartChar(R.ch)) {
            ProcessError(R.getLine(), "name character expected"); return "";
        }
        StringBuffer s = new StringBuffer();
        int pos = 0;
        while(NameChar(R.ch)) {
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

        // skip trailing white space
        R.skip();
        return s.toString();
    }

    private String scanNameOrNumber() throws IOException {
        StringBuffer s = new StringBuffer();
        int pos = 0;
        while(NameChar(R.ch)) {
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

        // skip trailing white space
        R.skip();
        return s.toString();
    }

    private webl.util.Set scanNames() throws IOException {
        webl.util.Set S = new webl.util.Set();

        if (R.ch == '(') {
            R.get();
            S.put(scanName().toLowerCase());
            while (R.ch == '|') {
                R.get();
                S.put(scanName().toLowerCase());
            }
            expect(')');
            R.skip();
        } else
            S.put(scanName().toLowerCase());
        return S;
    }

    private String scanLit() throws IOException {
        if (R.ch == '"' || R.ch == '\'') {
            int fin = R.ch;
            int linestart = R.getLine();
            R.adv();

            StringBuffer s = new StringBuffer();
            int pos = 0;
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

    private void expect(int line, int sym) throws IOException {
        if (R.ch == sym)
            R.adv();
        else
            ProcessError(line, (char)sym + " expected, found \"" + (char)(R.ch) + "\" instead");
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


    void ProcessError(int lineno, String s) {
        Log.debugln("[ line " + lineno + ": " + s + "]");
    }

}


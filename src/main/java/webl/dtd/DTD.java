package webl.dtd;

import java.util.*;
import java.io.*;
import webl.util.Log;

public class DTD
{
    private Hashtable<String, String> charentities   = new Hashtable<String, String>();
    private Hashtable<String, String> macroentities  = new Hashtable<String, String>();
    private Hashtable<String, DTDElement> elements       = new Hashtable<String, DTDElement>();

    public DTD () {
    }

    public DTDElement getElement(String name) {
        DTDElement element = elements.get(name);
        if (element != null)
            return element;
        else
            return null;
    }

    public void addElement(DTDElement e) {
        // only add an element if it does not exist already (SGML spec)
        DTDElement element = elements.get(e.getName());
        if (element == null) {
            elements.put(e.getName(), e);
        }
    }

    public void addCharEntity(String name, String value) {
        String entity = charentities.get(name);
        if (entity == null)
            charentities.put(name, value);
    }

    public void addMacroEntity(String name, String value) {
        name = name.toLowerCase();
        String macroEntity = macroentities.get(name);
        if (macroEntity == null)
            macroentities.put(name, value);
    }

    public Hashtable getMacroEntities() {
        return macroentities;
    }

    public Hashtable getCharEntities() {
        return charentities;
    }

    public String toString() {
        String eol = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer();

        Enumeration enumeration = elements.elements();
        while (enumeration.hasMoreElements()) {
            buf.append(enumeration.nextElement().toString()).append(eol);
        }
        return buf.toString();
    }

    public String ExpandCharEntities(String s) {
        try {
            StringBuffer buf = new StringBuffer();
            Reader R = new BufferedReader(new StringReader(s));
            int ch = R.read();
            while (ch != -1) {
                if (ch == '&') {
                    ch = R.read();
                    if (ch == '#') {
                        ch = R.read();
                        if (Digit(ch)) {
                            int no = 0;
                            while (Digit(ch)) {
                                no = no * 10 + ch - '0';
                                ch = R.read();
                            }
                            buf.append((char)no);
                            if (ch == ';')
                                ch = R.read();
                        } else
                            buf.append("&#");
                    } else if (Letter(ch)) {
                        StringBuffer E = new StringBuffer();
                        while(Letter(ch) || Digit(ch)) {
                            E.append((char)ch);
                            ch = R.read();
                        }
                        buf.append((char)LookupCharEntity(E.toString()));
                        if (ch == ';')
                            ch = R.read();
                    } else
                        buf.append('&');
                } else {
                    buf.append((char)ch);
                    ch = R.read();
                }
            }
            return buf.toString();
        } catch (IOException e) {
            throw new InternalError("ExpandCharEntities " + e);
        }
    }

    public int LookupCharEntity(String e) throws IOException {
        String s = (String)charentities.get(e);
        if (s != null) {
            Reader R = new StringReader(s);
            int no = 0;
            int ch = R.read();
            while (ch != -1) {
                if (Digit(ch))
                    no = no * 10 + ch - '0';
                ch = R.read();
            }
            return no;
        } else if (e.equals("gt"))
            return '>';
        else if (e.equals("lt"))
            return '<';
        else if (e.equals("amp"))
            return '&';
        else                        // unknown entity
            return '?';
    }

    private boolean Letter(int ch) {
        return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
    }

    private boolean Digit(int ch) {
        return ch >= '0' && ch <= '9';
    }
}

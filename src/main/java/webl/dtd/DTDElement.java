package webl.dtd;

import java.util.*;
import webl.util.*;

public class DTDElement
{
    String name;
    boolean optopen, optclose;
    webl.util.Set children, exclusions, inclusions;
    Hashtable<String, DTDAttribute> attrs = new Hashtable<String, DTDAttribute>();

    public DTDElement(String name, boolean optopen, boolean optclose, webl.util.Set children,
            webl.util.Set exclusions, webl.util.Set inclusions) {
        this.name = name;
        this.optopen = optopen;
        this.optclose = optclose;
        this.children = children;
        this.exclusions = exclusions;
        this.inclusions = inclusions;
    }

    public String getName() {
        return name;
    }

    public void addAttribute(DTDAttribute A) {
        attrs.put(A.name, A);
    }

    public DTDAttribute getAttribute(String name) {
        DTDAttribute obj = attrs.get(name);
        if (obj != null)
            return obj;
        else
            return null;
    }

    public boolean EmptyElement() {
        return children.getSize() == 0;
    }

    public boolean validChild(String name) {
        return (children.contains(name) || (inclusions != null && inclusions.contains(name)) || children.contains("any"))
            && (exclusions == null || !exclusions.contains(name));
    }

    public boolean optionalEndTag() {
        return optclose;
    }

    private void dump(StringBuffer buf, webl.util.Set S, String connect) {
        buf.append("(");
        Enumeration enumeration = S.elements();
        while (enumeration.hasMoreElements()) {
            String n = (String)enumeration.nextElement();
            buf.append(n);
            if (enumeration.hasMoreElements())
                buf.append(connect);
        }
        buf.append(")");
    }

    public String toString() {
        String eol = System.getProperty("line.separator");
        Enumeration enumeration;

        StringBuffer buf = new StringBuffer();
        buf.append("<!ELEMENT ").append(name).append(' ');
        if (optopen)
            buf.append("O ");
        else
            buf.append("- ");

        if (optclose)
            buf.append("O ");
        else
            buf.append("- ");

        /*Emit the simplified content model*/
        if (children.getSize() > 0) {
            buf.append(" ");
            dump(buf, children, ", ");
            buf.append("* ");
        } else
            buf.append("EMPTY");

        if (exclusions != null) {
            buf.append(" -");
            dump(buf, exclusions, ", ");
            buf.append(") ");
        }

        if (inclusions != null) {
            buf.append(" +");
            dump(buf, inclusions, ", ");
            buf.append(" ");
        }

        buf.append(">");

        if (attrs.size() > 1) {
            buf.append(eol).append("<!ATTLIST ").append(name).append(eol);
            enumeration = attrs.elements();
            while (enumeration.hasMoreElements()) {
                DTDAttribute A = (DTDAttribute)enumeration.nextElement();
                buf.append("   ").append(A.name).append(' ');
                if (A.type instanceof String)
                    buf.append((String)A.type);
                else {
                    dump(buf, (webl.util.Set)A.type, "|");
                }
                buf.append(' ');
                if (A.fixed) {
                    buf.append("#FIXED ").append(A.defval);
                } else
                    buf.append(A.defval);
                buf.append(eol);
            }
            buf.append(">");
        }
        return buf.toString();
    }
}

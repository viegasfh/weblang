package webl.page;

import java.util.regex.*;
import webl.lang.*;
import webl.lang.expr.*;
import webl.dtd.*;
import webl.util.*;
import java.util.*;
import java.io.*;

public class Page extends ObjectExpr
{
    public static final int  HTML = 1, XML = 2;
    public int          format;         // format of the page, XML or HTML
    public DTD          dtd;            // the DTD (if any)

    public  Elem        head;           // sequence of Elems this page contains
    private PieceSet    pieces;         // pieces belonging to this page

    private int cleanupcounter = 0;     // > 0 means that there are some tags that can be removed from the page

    public Page(DTD dtd, int format) {
        super();
        this.dtd = dtd;
        this.format = format;

        head = new Str("");
        pieces = new PieceSet(this);
    }

    public String getTypeName() {
        return "page";
    }

    synchronized final public void appendPCData(String s) {
        Elem t = new Str(s);
        insertElemAfter(head.prev, t, -1);
    }

    synchronized final public Piece appendOpenTag(String name) {
        Piece p = new Piece(this, name);
        Tag t = new Tag(p);
        p.setBeg(t);
        insertElemAfter(head.prev, t, -1);

        pieces.append(p);
        return p;
    }

    synchronized final public void makeEmptyTag(Piece p) {
        p.setEnd(p.beg);
    }

    synchronized final public void appendCloseTag(Piece p) {
        Tag t = new Tag(p);
        p.setEnd(t);
        insertElemAfter(head.prev, t, -1);
    }

    synchronized final public Piece appendComment(String val) {
        Piece p = new CommentPiece(this, val);
        Tag t = new Tag(p);
        p.setBeg(t); p.setEnd(t);
        insertElemAfter(head.prev, t, -1);
        pieces.append(p);
        return p;
    }

    synchronized final public Piece appendPI(String target, String content) {
        Piece p = new PIPiece(this, target, content);
        Tag t = new Tag(p);
        p.setBeg(t); p.setEnd(t);
        insertElemAfter(head.prev, t, -1);
        pieces.append(p);
        return p;
    }

    synchronized final public Piece appendDoctype(String content) {
        Piece p = new DoctypePiece(this, content);
        Tag t = new Tag(p);
        p.setBeg(t); p.setEnd(t);
        insertElemAfter(head.prev, t, -1);
        pieces.append(p);
        return p;
    }

    synchronized final public Piece appendCData(String content) {
        Piece p = new CDataPiece(this, content);
        Tag t = new Tag(p);
        p.setBeg(t); p.setEnd(t);
        insertElemAfter(head.prev, t, -1);
        pieces.append(p);
        return p;
    }

    final public void ScheduleCleanup() {
        cleanupcounter++;
    }

    synchronized final public PieceSet getElem(String name) {
        Cleanup();                                  // check if we need to do some page scrubbing
        return PieceSet.OpElem(pieces, name);
    }

    synchronized final public Piece getFirstElem(String name) {
        Cleanup();                                  // check if we need to do some page scrubbing
        return PieceSet.OpFirstElem(pieces, name);
    }

    // all pieces with class c
    synchronized final public PieceSet getElemByClass(String c) {
        Cleanup();                                  // check if we need to do some page scrubbing
        return PieceSet.OpElemByClass(pieces, c);
    }

    // Get the single element by Id from the whole page
    synchronized final public Piece getElemById(String pieceId) {
        Cleanup();                                  // check if we need to do some page scrubbing
        return PieceSet.OpElemById(pieces, pieceId);
    }

    // get all the pieces of the page
    synchronized final public PieceSet getElem() {
        return pieces.OpClone();
    }


    // all the pieces in p with a specific name
    synchronized final public PieceSet getElem(Piece p, String name) throws TypeCheckException {  // all the pieces in p named name
        CheckCompatible(this, p);
        return PieceSet.OpSelect(pieces, p, name);
    }

    // the first piece in p with a specific name
    synchronized final public Piece getFirstElem(Piece p, String name) throws TypeCheckException {  // the first piece in p named name
        CheckCompatible(this, p);
        return PieceSet.OpSelectFirst(pieces, p, name);
    }

    // all the pieces in p with a specific name
    synchronized final public PieceSet getElemByClass(Piece p, String c) throws TypeCheckException {  // all the pieces in p with class c
        CheckCompatible(this, p);
        return PieceSet.OpSelectByClass(pieces, p, c);
    }

    // all the pieces in p
    synchronized final public PieceSet getElem(Piece p) throws TypeCheckException {
        CheckCompatible(this, p);
        return PieceSet.OpSelect(pieces, p);
    }

    // split the PCData Elem p into the form x T y, where x and y are PC data Elems, and T (the return
    // value) is an HTML tag. xlen is the length of the x pc data segment.
    synchronized final private Tag split(Str p, int xlen) {
        Tag T = new Tag(null);
        if (xlen == 0)
            insertElemBefore(p, T);
        else if (xlen == p.charwidth)
            insertElemAfter(p, T, -1);
        else if (xlen > 0 && xlen < p.charwidth) {
            String s = p.getPCData();
            p.setPCData(s.substring(0, xlen));
            insertElemAfter(p, T, -1);
            insertElemAfter(T, new Str(s.substring(xlen)), -1);
        } else
            throw new Error("out of range xlen=" + xlen + " charwidth=" + p.charwidth);
        return T;
    }

    synchronized final public void deleteRange(Piece p) throws TypeCheckException {
        CheckCompatible(this, p);
        Elem rn = p.beg.prev;

        Elem x = p.beg;
        while (true) {
            Elem nxt = x.next;                             // save away the next element

            // get rid of the elem
            if (x instanceof Str)
                removeElem(x);
            else {                                                              // Tag
                Piece owner = ((Tag)x).getOwner();
                if (owner != null) {
                    if (owner.beg == x && owner.end.sno <= p.end.sno)             // begin tag
                        anonymize(owner);
                    else if (owner.end == x && owner.beg.sno >= p.beg.sno)        // end tag
                        anonymize(owner);
                }
            }
            if (x == p.end) break;
            x = nxt;
        }
    }

    synchronized final public void deleteRange(PieceSet s) throws TypeCheckException {
        CheckCompatible(this, s);

        Cell p = s.head.next;
        while (p != s.head) {
            deleteRange(p.pce);
            p = p.next;
        }
        // verify();
    }

    // member class to store a list of Elems
    private class ElemBuffer {
        Elem head, tail;
        int count = 0;

        public void put(Elem e) {
            e.next = null;
            if (head == null)
                head = e;
            else
                tail.next = e;
            tail = e;
            count++;
        }

        public Elem get() {
            Elem r = head;
            if (r != null) {
                head = head.next;
                r.next = r; r.prev = r;
                count--;
            }
            return r;
        }

        public int size() {
            return count;
        }
    }

    static Counter cia = new Counter("webl.lang.page.insertAfter");

    synchronized final public Elem insertAfter(Elem pos, Piece p) {
// In the first pass we just make copies of everything in p
        cia.begin();      //////////////////////

        webl.util.PriorityQueue begQ = new webl.util.PriorityQueue();
        webl.util.PriorityQueue endQ = new webl.util.PriorityQueue();
        Hashtable H = new Hashtable();
        ElemBuffer B = new ElemBuffer();
        Vector N = new Vector();

        Elem x = p.beg;
        while (true) {
            Elem nxt = x.next;

            if (x instanceof Str) {
                Elem t = new Str( ((Str)x).getPCData() );
                B.put(t);
            } else {       // Tag
                Tag tagx = (Tag)x;
                Piece owner = tagx.getOwner();
                if (owner != null) {                                    // only copy named tags
                    if (owner.beg == x) {                               // open tag
                        Piece np = (Piece)(owner.clone());
                        np.page = this;
                        Tag t = new Tag(np);
                        np.setBeg(t);
                        B.put(t);
                        if (owner.end == x) {
                            np.setEnd(t);
                        } else {
                            H.put(owner, np);
                            if (owner.end.sno > p.end.sno)
                                endQ.put(np, tagx.sno);
                        }
                        N.addElement(np);
                    } else if (owner.end == x) {                        // close tag
                        Object o = H.get(owner);
                        if (o != null) {                                // matching begin tag
                            Piece np = (Piece)o;
                            Tag t = new Tag(np);
                            np.setEnd(t);
                            B.put(t);
                            // endQ.remove(np);
                        } else {                                        // no matching begin tag
                            Piece np = (Piece)(owner.clone());
                            np.page = this;
                            Tag t = new Tag(np);
                            np.setEnd(t);
                            B.put(t);
                            begQ.put(np, tagx.sno);
                            N.addElement(np);
                        }
                    } else
                        throw new Error("internal error");
                }
            }
            if (x == p.end) break;
            x = nxt;
        }

// Now perform the actual copying of the elements ...
        int elemcount = begQ.size() + B.size() + endQ.size();

        ElemWriter W = new ElemWriter(pos, elemcount);

        // fix up the orphan end tags
        Object o = begQ.get();
        while (o != null) {
            Piece pp = (Piece)o;

            Tag t = new Tag(pp);
            pp.setBeg(t);
            W.write(t);
            o = begQ.get();
        }

        // insert the saved buffer
        Elem k = B.get();
        while (k != null) {
            W.write(k);
            k = B.get();
        }

        // fix up the orphan begin tags
        o = endQ.get();
        while (o != null) {
            Piece pp = (Piece)o;

            Tag t = new Tag(pp);
            pp.setEnd(t);
            W.write(t);
            o = endQ.get();
        }

        try {
            Enumeration enumeration = N.elements();
            while (enumeration.hasMoreElements()) {
                Piece pp = (Piece)(enumeration.nextElement());
                pieces.insert(pp);
            }
        } catch (TypeCheckException e) {
            throw new Error("internal error");
        }

        cia.end();           //////////////////////////////////

        // verify();

        return W.getPos();
    }

    synchronized final public void insertBefore(Elem pos, Piece p) {
        insertAfter(pos.prev, p);
    }

    synchronized final public Elem insertAfter(Elem elem, PieceSet s) {
        Elem last = elem;

        Cell p = s.head.next;
        while (p != s.head) {
            last = insertAfter(last, p.pce);
            p = p.next;
        }
        return last;
    }

    synchronized final public void replace(PieceSet oldset, PieceSet newset) throws TypeCheckException {
        CheckCompatible(this, oldset);

        Cell p = oldset.head.next;
        while (p != oldset.head) {
            deleteRange(p.pce);
            insertAfter(p.pce.beg, newset);
            p = p.next;
        }
    }

    synchronized final public Piece getContentPiece(Elem beg, Elem end) {
        if (beg == end && beg != head) {
            return null;
        } else {
            Piece p = new Piece(this);

            Tag x = new Tag(null);
            insertElemAfter(beg, x, -1);
            p.setBeg(x);

            Tag y = new Tag(null);
            insertElemBefore(end, y);
            p.setEnd(y);

            return p;
        }
    }

    final public Piece getContentPiece() {
        return getContentPiece(head, head);
    }

    final public Piece getContentPiece(Piece p) {
        return getContentPiece(p.beg, p.end);
    }

    synchronized final public String getText(Elem beg, Elem end) {
        StringBuffer buf = new StringBuffer();

        Elem x = beg;
        while(true) {
            if (x instanceof Str) {
                buf.append(((Str)x).getPCData());
            }
            if (x == end) break;
            x = x.next;
        }
        return buf.toString();
    }

    final public String getText(Piece p) {
        return getText(p.beg, p.end);
    }

    final public String getText() {
        return getText(head.next, head.prev);
    }

    final Piece NewPiece(Tag beg, Tag end) {
        Piece p = new Piece(this);
        p.setBeg(beg);
        p.setEnd(end);
        return null;
    }

    synchronized final public String getMarkup(Elem beg, Elem end) {
        StringBuffer buf = new StringBuffer();

        Elem x = beg;
        while(true) {
            if (x instanceof Str)
                buf.append(((Str)x).getPCData());
            else {
                Piece owner = ((Tag)x).getOwner();
                if (owner != null) {                        // only use named pieces
                    if (owner.beg == x)
                        owner.writeOpenTag(buf);
                    else if (owner.end == x)
                        owner.writeCloseTag(buf);
                }
            }
            if (x == end) break;
            x = x.next;
        }
        return buf.toString();
    }

    final public String getMarkup(Piece p) {
        return getMarkup(p.beg, p.end);
    }

    final public String getMarkup() {
        return getMarkup(head.next, head.prev);
    }


    private class ParaBuffer
    {
        private PieceSet R;
        private Elem beg, end;

        public ParaBuffer(PieceSet R) {
            this.R = R;
        }

        public void Write(Elem x) {
            if (beg == null)
                beg = x;
            end = x;
        }

        public void Break() {
            if (beg != null && !Empty(beg, end))
                    R.append(SpecialPiece(beg, end));
            beg = end = null;
        }

        private boolean Empty(Elem beg, Elem end) {
            Elem x = beg;
            while(x != head) {
                if (x instanceof Str) {
                    if (!Empty( ((Str)x).getPCData() ))
                        return false;
                } else {
                    Piece owner = ((Tag)x).getOwner();
                    if (owner != null)
                        return false;
                }
                if (x == end) break;
                x = x.next;
            }
            return true;
        }

        private boolean Empty(String s) {
            int len = s.length();
            for (int i = 0; i < len; i++) {
                char ch = s.charAt(i);
                if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t' || ch == 160 || ch == '\f')
                    continue;
                return false;
            }
            return true;
        }

        public PieceSet Result() {
            return R;
        }
    }

    // do not include beg and end
    synchronized final public PieceSet getPara(Elem beg, Elem end, String breaktags) {
        boolean breakon = true;

        webl.util.Set breakset = new webl.util.Set();
        StringTokenizer T = new StringTokenizer(breaktags, " \n\r\t\f");
        if (T.hasMoreTokens()) {
            String s = T.nextToken();
            if (s.equals("-"))
                breakon = false;
            else
                breakset.put(s);
        }
        while (T.hasMoreTokens())
            breakset.put(T.nextToken());

        PieceSet R = new PieceSet(this);
        if (beg != head && beg == end)         //  empty element
            return R;

        ParaBuffer B = new ParaBuffer(R);

        Elem x = beg.next;
        while (x != end) {
            if (x instanceof Str) {             // write into buffer
                B.Write(x);
            } else {
                Piece owner = ((Tag)x).getOwner();
                if (owner == null) {            // unnamed tag, write into buffer
                    B.Write(x);
                } else if (breakon && breakset.contains(owner.name)) {   // break here
                    B.Break();
                } else if (!breakon && !breakset.contains(owner.name)) {    // break here
                    B.Break();
                } else {                        // non-break tag, write into buffer
                    B.Write(x);
                }
            }
            x = x.next;
        }
        B.Break();
        return B.Result();
    }

    final public PieceSet getPara(Piece p, String breaktags) {
        return getPara(p.beg, p.end, breaktags);
    }

    final public PieceSet getPara(String breaktags) {
        return getPara(head, head, breaktags);
    }


    void indent(StringBuffer buf, int lev) {
        while (lev-- > 0) buf.append("    ");
    }

    void blockprint(StringBuffer buf, int lev, String s) {
        int pos = 0, line = 0;
        int len = s.length();
        String eol = System.getProperty("line.separator");

        indent(buf, lev);
        while (pos < len) {
            char ch = s.charAt(pos++);

            // skip newlines
            if (ch == '\r' || ch == '\n')
                continue;

            // skip spaces at the beginning of the line
            if (line == 0 && ch <= ' ')
                continue;

            if (line > 50 && ch == ' ') {       // line too long
                buf.append(eol);
                indent(buf, lev);
                line = 0;
            } else {
                buf.append(ch);
                line++;
            }
        }
        buf.append(eol);
    }

    synchronized final public String getPrettyMarkup(Elem beg, Elem end) {
        String eol = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer();
        int prenesting = 0;

        int lev = 0;

        Elem x = beg;
        while(true) {
            if (x instanceof Str) {
                if (prenesting == 0)
                    blockprint(buf, lev, ((Str)x).getPCData());
                else
                    buf.append(((Str)x).getPCData());
            } else {
                Piece owner = ((Tag)x).getOwner();
                if (owner != null) {                        // only use named pieces
                    if (owner.beg == x) {
                        indent(buf, lev);
                        owner.writeOpenTag(buf);
                        buf.append(eol);
                        if (owner.end != x) lev++;
                        if (owner.name.equalsIgnoreCase("pre")) prenesting++;
                    } else if (owner.end == x) {
                        lev--;
                        indent(buf, lev);
                        owner.writeCloseTag(buf);
                        buf.append(eol);
                        if (owner.name.equalsIgnoreCase("pre")) prenesting--;
                    }
                }
            }
            if (x == end) break;
            x = x.next;
        }
        return buf.toString();
    }

    final public String getPrettyMarkup(Piece p) {
        return getPrettyMarkup(p.beg, p.end);
    }

    final public String getPrettyMarkup() {
        return getPrettyMarkup(head.next, head.prev);
    }

    static private Counter cpat = new Counter("Pat() searching");

    synchronized final public PieceSet getPattern(String regexp) throws PatternSyntaxException {

        Cleanup();                      // check if we need to do some page scrubbing

        cpat.begin();


        PieceSet R = new PieceSet(this);
        Elem p = head.next;
        int pos = 0;

        try {
            Pattern        pattern = Pattern.compile(regexp);
            // get the PageReader into a Buffer
            String pageStr = pageToString(new PageReader(this));
            Matcher matcher  = pattern.matcher(pageStr);
            MatchResult result = null;
            // here we will try to find as many matches
            // as we can in the whole page buffered into a string
            // The Perl5StreamInput buffers the bytes read and keeps
            // track of the position within the whole stream
            // As a quick hack I converted the whole page into a 
            // string. This way, we can keep track of the positions within
            // the whole string
            while (matcher.find()) {
                result = matcher.toMatchResult();
                int beg = result.start();
                int end = result.end();

                if (beg < end && beg >= pos) {
                    // Note: I added beg >= pos because OROMatch seems to have a bug that sometimes returns
                    // a previously calculated position. (?)

                    // locate the begin position
                    while (p != head && (p instanceof Tag || beg >= pos + p.charwidth)) {
                        pos += p.charwidth; p = p.next;
                    }
                    if (p == head) throw new Error("out of range");
                    Tag x = split((Str)p, beg - pos);
                    p = x; pos = beg;

                    // locate the end position
                    while (p != head && end > pos + p.charwidth) {
                        pos += p.charwidth; p = p.next;
                    }
                    if (p == head) throw new Error("out of range");

                    Tag y = split((Str)p, end - pos);
                    p = y; pos = end;

                    Piece np = new Piece(this);
                    np.setBeg(x); np.setEnd(y);
                    R.append(np);

                    int groups = result.groupCount() + 1;
                    for (int g = 0; g < groups; g++) {
                        String s = result.group(g);
                        if (s == null) s = "";
                        np.def(Program.Int(g), Program.Str(s));
                    }
                } // else empty string
            }
        } catch(IOException e) {
        }

        cpat.end();
        // verify();
        return R;
    }

    synchronized final public PieceSet getPattern(Piece piece, String regexp) throws PatternSyntaxException, TypeCheckException {
        CheckCompatible(this, piece);
        PieceSet R = new PieceSet(this);
        if (piece.beg == piece.end)
            return R;

        Cleanup();                      // check if we need to do some page scrubbing

        cpat.begin();
        Elem p = head.next;
        int pos = 0;
        while (p != head && p != piece.beg) {
            pos += p.charwidth;
            p = p.next;
        }
        if (p == head)
            throw new Error("internal error");

        // now p = piece.beg, and p is a tag
        int skew = pos;

        try {
            Pattern        pattern = Pattern.compile(regexp);
            String pageStr = pageToString(new PageReader(this, p, piece.end));
            Matcher        matcher = pattern.matcher(pageStr);
            MatchResult         result = null;           
            outerloop:
            while (p != piece.end && matcher.find()) {
                result = matcher.toMatchResult();
                int beg = result.start() + skew;
                int end = result.end() + skew;
                if (beg < end && beg >= pos) {
                    // locate the begin position
                    while (p != head && (p instanceof Tag || beg >= pos + p.charwidth)) {
                        pos += p.charwidth; p = p.next;
                        if (p == piece.end) break outerloop;
                    }
                    if (p == head) throw new Error("out of range");
                    Tag x = split((Str)p, beg - pos);
                    p = x; pos = beg;

                    // locate the end position
                    while (p != head && end > pos + p.charwidth) {
                        pos += p.charwidth; p = p.next;
                        if (p == piece.end) break outerloop;
                    }
                    if (p == head) throw new Error("out of range");

                    Tag y = split((Str)p, end - pos);
                    p = y; pos = end;

                    Piece np = new Piece(this);
                    np.setBeg(x); np.setEnd(y);
                    R.append(np);

                    int groups = result.groupCount() + 1;
                    for (int g = 0; g < groups; g++) {
                        String s = result.group(g);
                        if (s == null) s = "";
                        np.def(Program.Int(g), Program.Str(s));
                    }
                } // else empty string
            }
        } catch(IOException e) {
        }
        cpat.end();
        return R;
    }

    synchronized final public PieceSet Chop(TextChopper chopper) {
        PieceSet R = new PieceSet(this);

        Cleanup();

        Elem p = head.next;
        int pos = 0;

        while (chopper.nextTag()) {
            int beg = chopper.getPosition();

            // locate the begin position
            if (chopper.isBeginTag()) {
                while (p != head && (p instanceof Tag || beg >= pos + p.charwidth)) {
                    pos += p.charwidth; p = p.next;
                }
            } else {
                while (p != head && (p instanceof Tag || beg > pos + p.charwidth)) {
                    pos += p.charwidth; p = p.next;
                }
            }

            if (p == head) throw new Error("out of range");
            Tag x = split((Str)p, beg - pos);
            p = x; pos = beg;

            if (chopper.isBeginTag()) {
                Piece np = new Piece(this);
                np.setBeg(x);
                np.def("label", Program.Str(chopper.getName()));
                Object handle = chopper.getHandle();
                hash.put(handle, np);
            } else {
                Piece np = (Piece)hash.get(chopper.getOtherHandle());
                np.setEnd(x);
                try {
                    R.insert(np);
                } catch (TypeCheckException e) {
                    throw new Error("Panic: unexpected type check exception");
                }
            }
        }
        return R;
    }


    synchronized public Piece SpecialPiece(Elem x, Elem y) {
        if (x == head || y == head)
            throw new Error("internal error");
        if (x.sno > y.sno) {            // swap x and y to place them in sequence
            Elem t = x;
            x = y;
            y = t;
        }
        Piece p = new Piece(this);
        Tag x0 = new Tag(null);
        p.setBeg(x0);
        insertElemBefore(x, x0);

        Tag y0 = new Tag(null);
        p.setEnd(y0);
        insertElemAfter(y, y0, -1);

        return p;
    }

    synchronized public Piece SpecialPiece(String name, Elem x, Elem y) {
        if (name.equals(""))
            return SpecialPiece(x, y);

        if (x == head || y == head)
            throw new Error("internal error");
        if (x.sno > y.sno) {            // swap x and y to place them in sequence
            Elem t = x;
            x = y;
            y = t;
        }
        Piece p = new Piece(this, name);
        Tag x0 = new Tag(p);
        p.setBeg(x0);
        insertElemBefore(x, x0);

        Tag y0 = new Tag(p);
        p.setEnd(y0);
        insertElemAfter(y, y0, -1);

        try {
            pieces.insert(p);
        } catch (TypeCheckException e) {
            throw new InternalError("SpecialPiece");
        }
        return p;
    }

    synchronized PieceSet Flatten(PieceSet x) {
        PieceSet R = new PieceSet(this);
        Cell p = x.head.next;
        Cell pend = x.head;

        Elem left = null, right = null;
        while (p != pend) {
            if (left == null) {
                left = p.pce.beg;
                right = p.pce.end;
            } else if (p.pce.beg.sno > right.sno) {
                R.append(SpecialPiece(left, right));
                left = p.pce.beg;
                right = p.pce.end;
            } else if (p.pce.end.sno >= right.sno) {
                // determine the "right-most" of p.pce.end and right
                Elem candidate = p.pce.end;
                Elem k = candidate;
                while(k.sno == right.sno) {
                    if (k == right)
                        candidate = right;
                    k = k.next;
                }
                right = candidate;

                // determine the "left-most" of p.pce.beg and left
                candidate = p.pce.beg;
                k = candidate;
                while(k.sno == right.sno) {
                    if (k == left)
                        candidate = left;
                    k = k.prev;
                }
                left = candidate;
            }
            p = p.next;
        }
        if (left != null)
            R.append(SpecialPiece(left, right));
        return R;
    }

    synchronized PieceSet Without(PieceSet x, PieceSet y) {
        PieceSet R = new PieceSet(this);
        Cell p = x.head.next;
        Cell q = y.head.next;

        Cell pend = x.head;
        Cell qend = y.head;

        while (p != pend) {
            if (q == qend) {
                R.append(p.pce);
                p = p.next;
            } else if (Piece.cbefore(p.pce, q.pce)) {
                R.append(p.pce);
                p = p.next;
            } else if (Piece.cbefore(q.pce, p.pce)) {
                q = q.next;
            } else {        // some overlapping occurs
                // set up candidate
                Elem cbeg = p.pce.beg;
                Elem cend = p.pce.end;
                boolean insert = true;

                Cell r = q;
                while (r != qend && r.pce.beg.sno <= cend.sno) {
                    Piece rr = r.pce;

                    if (cbeg.sno < rr.beg.sno) {
                        R.append(SpecialPiece(cbeg, rr.beg.prev));
                        if (cend.sno <= rr.end.sno) {
                            insert = false;
                            break;
                        } else
                            cbeg = rr.end.next;
                    } else {
                        if (cend.sno <= rr.end.sno) {
                            insert = false;
                            break;
                        } else
                            cbeg = rr.end.next;
                    }
                    r = r.next;
                }
                if (insert) {
                    R.append(SpecialPiece(cbeg, cend));
                }
                p = p.next;
            }
        }
        return R;
    }

    synchronized PieceSet RegionIntersect(PieceSet x, PieceSet y) {
        PieceSet R = new PieceSet(this);
        Cell p = x.head.next;
        Cell q = y.head.next;

        Cell pend = x.head;
        Cell qend = y.head;

        while (p != pend) {
            if (q == qend) {
                p = p.next;
            } else if (Piece.cbefore(p.pce, q.pce)) {
                p = p.next;
            } else if (Piece.cbefore(q.pce, p.pce)) {
                q = q.next;
            } else {        // some overlapping occurs
                // set up candidate
                Elem cbeg = p.pce.beg;
                Elem cend = p.pce.end;

                Cell r = q;
                while (r != qend && r.pce.beg.sno <= cend.sno) {
                    Piece rr = r.pce;
                    if (cbeg.sno < rr.beg.sno)
                        cbeg = rr.beg;
                    if (rr.end.sno < cend.sno && rr.end.sno >= cbeg.sno)
                        cend = rr.end;
                    r = r.next;
                }
                R.append(SpecialPiece(cbeg, cend));
                p = p.next;
            }
        }
        return R;
    }

    private boolean StrOrAnonPiece(Elem x) {
        return (x instanceof Str) || ((Tag)x).getOwner() == null;
    }

    private boolean AnonPiece(Elem x) {
        return (x instanceof Tag) && ((Tag)x).getOwner() == null;
    }

    synchronized final public PieceSet Children(Piece p) throws TypeCheckException {
        CheckCompatible(this, p);
        PieceSet R = new PieceSet(this);

        Elem x = p.beg;
        if (x != p.end)
            x = x.next;
        while(x != p.end && x.sno <= p.end.sno) {
            if (x instanceof Str) {
                Elem a = x;
                Elem b = x;
                while (x != p.end && StrOrAnonPiece(x)) {
                    b = x;
                    x = x.next;
                }
                R.append(SpecialPiece(a, b));
            } else {
                Tag T = (Tag)x;
                Piece owner = T.getOwner();
                if (owner != null) {
                    if (owner.beg == T) {   // begin tag
                        if (owner.end.sno <= p.end.sno) {
                            R.append(owner);
                            x = owner.end;
                        } // else not a valid child
                    } else if (owner.end == T) {  // end tag
                        if (owner.beg.sno >= p.beg.sno)    // nested child
                            R.append(owner);
                    } else
                        throw new Error("internal error");
                }
                x = x.next;
            }
            if (x == p.end || x.sno >= p.end.sno) break;
        }
        return R;
    }

    synchronized final public Piece Parent(Piece p) throws TypeCheckException {
        CheckCompatible(this, p);

        Elem x = p.beg.prev;
        while (x != head) {
            if (x instanceof Tag) {
                Piece owner = ((Tag)x).getOwner();
                if (owner != null && owner.end.sno >= p.end.sno)
                    return owner;
            }
            x = x.prev;
        }
        return null;
    }

    final private Elem SeqNext(Elem e) {
        if (e instanceof Tag) {
            Piece owner = ((Tag)e).getOwner();
            if (owner != null) {
                e = owner.end.next;
                while (AnonPiece(e))
                    e = e.next;
            } else
                throw new InternalError("SeqNext precondition failed");
        } else {        // Str
            e = e.next;
            while (StrOrAnonPiece(e))
                e = e.next;
        }
        return e;
    }

    final private boolean SeqMatch(Elem e, String pat) {
        if (e instanceof Str)
            return pat.equals("#");
        else {
            Piece owner = ((Tag)e).getOwner();
            if (owner == null)
                return false;
            else
                return owner.beg == e && owner.name.equals(pat);
        }
    }

    static Counter seqc = new Counter("Seq() matching");

    synchronized final public PieceSet FindSeq(String seq) {
        if (head.next == head)
            return new PieceSet(this);
        else
            return FindSeq(head.next, head.prev, seq);
    }

    synchronized final public PieceSet FindSeq(Piece p, String seq) {
        if (p.beg == p.end || p.beg.next == p.end)
            return new PieceSet(this);
        else
            return FindSeq(p.beg.next, p.end.prev, seq);
    }

    // inclusive beg and end
    synchronized final private PieceSet FindSeq(Elem beg, Elem end, String seq) {
        seqc.begin();

        PieceSet R = new PieceSet(this);

        StringTokenizer T = new StringTokenizer(seq, " ");
        String[] tag = new String[T.countTokens()];
        int i = 0;
        while (T.hasMoreTokens())
            tag[i++] = T.nextToken();
        if (i == 0) return R;

        int notags = i;

        Elem x = beg;

        outerloop:
        while(x != end) {
            if (SeqMatch(x, tag[0])) {
                Elem p = SeqNext(x);
                i = 1;
                while(p != head && p.sno <= end.sno && i < notags && SeqMatch(p, tag[i])) {
                    p = SeqNext(p);
                    i++;
                }
                if (i == notags) {  // complete match
                    Piece obj = SpecialPiece(x, p.prev);        // whole range
                    i = 0;
                    Elem y = x;
                    while (y != p) {
                        // obj[i] = x
                        Piece yp;
                        Elem nxt = SeqNext(y);
                        if (y instanceof Tag)
                            yp = ((Tag)y).getOwner();
                        else
                            yp = SpecialPiece(y, nxt.prev);
                        obj.def(Program.Int(i), yp);
                        y = nxt;
                        i++;
                    }
                    R.append(obj);
                }
                x = x.next;
            } else
                x = x.next;
        }
        seqc.end();
        return R;
    }

    synchronized final public void ProcessPage(PageProcessor P) {
        Elem p = head.next;
        while (p != head) {
            if (p instanceof Str) {
                P.Text((Str)p);
            } else {
                Piece owner = ((Tag)p).getOwner();
                if (owner != null) {                        // only use named pieces
                    if (owner.beg == p) {
                        if (owner.end == p)
                            P.BeginEndTag(owner);
                        else
                            P.BeginTag(owner);
                    } else if (owner.end == p) {
                        P.EndTag(owner);
                    }
                }
            }
            p = p.next;
        }
    }

    // include beg and end in calculation
    synchronized final public PieceSet GetPCDataPieces(Elem beg, Elem end) {
        PieceSet R = new PieceSet(this);

        Elem x = beg;
        while(true) {
            if (x instanceof Str) {
                Elem a = x;
                Elem b = x;
                while (StrOrAnonPiece(x)) {
                    b = x;
                    if (x == end) break;
                    x = x.next;
                }
                R.append(SpecialPiece(a, b));
                if (x == end) break;
            } else {
                if (x == end) break;
                x = x.next;
            }
        }
        return R;
    }

    synchronized final public PieceSet GetPCDataPieces(Piece p) throws TypeCheckException {
        CheckCompatible(this, p);
        return GetPCDataPieces(p.beg, p.end);
    }

    synchronized final public PieceSet GetPCDataPieces() {
        return GetPCDataPieces(head.next, head.prev);
    }

    synchronized final public void Cleanup() {
        if (cleanupcounter > 0) {
            cleanupcounter = 0;

            int c = 0;
            Elem p = head.next;
            while (p != head) {
                Elem nxt = p.next;         // squirrel the pointer away as we might overwrite it

                if (p instanceof Tag && ((Tag)p).getRefCount() == 0) {
                    if (((Tag)p).getOwner() != null)
                        throw new InternalError("attempting to scrub a tag with an owner");
                    else {
                        removeElem(p);
                        c++;
                    }
                }
                p = nxt;
            }
        }
    }

////////////// Stuff related to elem numbering

    static final long SPACE = 1L << 40;

    final boolean whiteElem(Elem p) {
        return p instanceof Tag && ((Tag)p).getOwner() == null;
    }

    final boolean inSeq(Elem p, Elem q) {
        if (whiteElem(p) && whiteElem(q))
            return p.sno == q.sno;
        else
            return q.sno > p.sno;
    }

    // verify that the elem numbering is correct
    synchronized final public void verify() {
        Elem p = head.next;
        while (p != head) {
            if (!inSeq(p.prev, p))
                throw new Error("verify failed");
            p = p.next;
        }
    }


    final private void renumerationberAllElems() {
        int renumerationbers = 0;

        Elem p = head.next;
        while (p != head) {
            if (whiteElem(p) && whiteElem(p.prev))
                p.sno = p.prev.sno;
            else
                p.sno = p.prev.sno + SPACE;
            renumerationbers++;
            p = p.next;
        }
    }

    final private void insertElemAfter(Elem x, Elem n, long hint) {
        if (n.next != n || n.prev != n)
            throw new Error("not a fresh Elem");

        Elem L = x;
        Elem R = x.next;

        n.prev = L; n.next = R;
        R.prev = n; L.next = n;

    // now pick a search number for n

        if (whiteElem(n)) {                 // make sure that consecutive white elems have the same number
            if (whiteElem(L)) {
                n.sno = L.sno;
                return;
            } else if (whiteElem(R)) {
                n.sno = R.sno;
                return;
            }
        }

        // at this point we have to assign a new number between L.sno and R.sno
        if (R == head)
            n.sno = L.sno + SPACE;
        else {
            long no;
            if (hint > L.sno && hint < R.sno)
                no = hint;
            else
                no = L.sno + (R.sno - L.sno) / 2;

            if (no == L.sno || no == R.sno)        // we have a conflict
                makeSpace(L, R);
            else
                n.sno = no;
        }
    }

    static Counter rencount = new Counter("local renumerationbers");

    final private void makeSpace(Elem L, Elem R) {
        rencount.begin();           //////////////

        int c = 1;

        boolean goleft, goright;

        goleft = (L != head);
        goright = (R != head && R.next != head);
        while ((goleft || goright) && (R.sno - L.sno < SPACE)) {
            if (goleft) {
                L = L.prev;
                c++;
            }
            if (goright) {
                R = R.next;
                c++;
            }
            goleft = (L != head);
            goright = (R != head && R.next != head);
        }

        if (c > 50)
            renumerationberAllElems();
        else {                  // renumerationber between L and R
            int renumerationbers=0;

            long inc;
            if (R.next == head)     // just as well make a large space
                inc = SPACE;
            else
                inc = (R.sno - L.sno) / (c + 1);

            Elem p = L.next;
            while (p != head && p != R) {
                if (whiteElem(p) && whiteElem(p.prev))
                    p.sno = p.prev.sno;
                else
                    p.sno = p.prev.sno + inc;
                renumerationbers++;
                p = p.next;
            }

            long no;

            // make sure the elem numbering invariant is still there

            if (whiteElem(p) && whiteElem(p.prev))
                no = p.prev.sno;
            else
                no  = p.prev.sno + inc;

            while (p != head && (p.sno < no || whiteElem(p))) {
                p.sno = no;
                renumerationbers++;

                p = p.next;

                if (whiteElem(p) && whiteElem(p.prev))
                    no = p.prev.sno;
                else
                    no  = p.prev.sno + inc;
            }

            rencount.end();         ////////////////////
        }

        // verify();       // debugging code
    }

    final private void insertElemBefore(Elem x, Elem n) {
        insertElemAfter(x.prev, n, -1);
    }

    final private void removeElem(Elem x) {
        if (x == head)
            throw new InternalError("cannot remove head");

        if (!x.Valid())
            throw new InternalError("trying to remove an invalid (not belonging to a page) Elem");

        Elem prev = x.prev;
        Elem next = x.next;

        prev.next = next; next.prev = prev;
        x.next = x; x.prev = x;

        if (whiteElem(prev) && whiteElem(next)) {
            long no = prev.sno;

            Elem p = next;
            while (p != head && whiteElem(p)) {
                p.sno = no;
                p = p.next;
            }
        }
    }

    final private void renumeration(Elem p) {
        long no;

        if (whiteElem(p.prev))
            no = p.prev.sno;
        else if (whiteElem(p.next))
            no = p.next.sno;
        else
            return;

        while (p != head && whiteElem(p) && p.sno != no) {
            p.sno = no;
            p = p.next;
        }
    }

    final private void anonymize(Piece p) throws TypeCheckException {
        pieces.remove(p);                           // piece will eventually be collected
        p.beg.setOwner(null);
        p.end.setOwner(null);
        renumeration(p.beg);
        renumeration(p.end);
    }

    private class ElemWriter {
        Elem pos;
        int count;

        public ElemWriter(Elem pos, int count) {
            this.pos = pos;
            this.count = count;
        }

        public void write(Elem n) {
            long hint = pos.sno + (pos.next.sno - pos.sno) / (count + 1);
            insertElemAfter(pos, n, hint);
            pos = n;
            count--;
        }

        public Elem getPos() {
            return pos;
        }
    }

/////// End of stuff related to tag numbering

    static void CheckCompatible(Page p, Piece x) throws TypeCheckException {
        if (p != x.page)
            throw new TypeCheckException("the piece does not belong to the page");
    }

    static void CheckCompatible(Page p, PieceSet x) throws TypeCheckException {
        if (p != x.page)
            throw new TypeCheckException("the pieceset does not belong to the page");
    }

    void flatprint(StringBuffer buf, String s) {
        int pos = 0, line = 0;
        int len = s.length();

        while (pos < len) {
            char ch = s.charAt(pos++);

            // skip newlines
            if (ch == '\r') {
                buf.append("\\r");
                continue;
            }

            if (ch == '\n') {
                buf.append("\\n");
                continue;
            }

            // skip spaces at the beginning of the line
            if (line == 0 && ch <= ' ')
                continue;

            if (line > 50 && ch == ' ') {       // line too long
                return;
            } else {
                buf.append(ch);
                line++;
            }
        }
    }

    synchronized public String StructureDump(Elem beg, Elem end) {
        String eol = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer();
        Elem x = beg;
        while(true) {
            buf.append(" sno=").append(x.sno).append(" ");
            if (x instanceof Str) {
                buf.append("str ");
                flatprint(buf, ((Str)x).getPCData());
            } else {
                Piece owner = ((Tag)x).getOwner();
                if (owner != null) {                        // only use named pieces
                    buf.append("tag ");
                    if (owner.beg == x) {
                        owner.writeOpenTag(buf);
                    } else if (owner.end == x) {
                        owner.writeCloseTag(buf);
                    }
                } else
                    buf.append("tag (anon)");
            }
            buf.append(eol);
            if (x == end) break;
            x = x.next;
        }
        return buf.toString();
    }

    // Converts a PageReader to a String, so that
    // we can apply the regular expressions to the whole
    // buffer and keep track of the positions within the 
    // stream (or string in this case). This was a quick hack
    // in converting the old Oro style Perl5StreamInput
    // We could emulate the Perl5StreamInput with the reader,
    // by keeping track of all the positions and continuing to 
    // buffer it, but it is more work, and I just wanted 
    // to get the code to work with Java 8. After having it
    // to work on Java 8, I will refactor the code with more efficient
    // and modern code constructs
    private String pageToString(PageReader pageReader) throws IOException {
        StringBuilder buffer = new StringBuilder(); 
        try (BufferedReader bufferedReader = new BufferedReader(pageReader)) {
           String line = null;

           while ((line = bufferedReader.readLine()) != null) {
             buffer.append(line);
           }
        }

        return buffer.toString();
    }
}


/////////////////////////////////////////////////////////////////////////////////////////


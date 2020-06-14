package webl.page;

import webl.lang.*;
import webl.lang.expr.*;
import webl.lang.builtins.*;
import webl.util.Log;

import java.lang.*;
import java.util.*;

public class TestHarness extends AbstractFunExpr
{
    static final int NOELEM = 500;
    
    public String toString() {
        return "<TestHarness>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        try {
            RunTests();
        } catch (TypeCheckException e) {
            throw new Error("internal error");
        }
        return Program.nilval;
    }
            
    public Page RandomPage() {
        Stack S = new Stack();
        Page P = new Page(null, Page.HTML);
        for (int i = 0; i < NOELEM; i++) {
            if (Prob(0.3))
                P.appendPCData("TEXT");
            else {
                if (!S.empty() && Prob(0.2)) {          // close a tag
                    if (Prob(0.6)) {
                        Piece pp = (Piece)(S.pop());
                        P.appendCloseTag(pp);
                    } else {                            // overlap
                        int e = (int)(Math.random() * S.size());
                        Piece pp = (Piece)(S.elementAt(e));
                        P.appendCloseTag(pp);
                        S.removeElementAt(e);
                    }
                } else {                                // open a new tag
                    Piece pp = P.appendOpenTag(RandomName());
                    S.push(pp);
                }
            }
        }
        while (!S.empty()) {                            // empty the stack
            Piece pp = (Piece)(S.pop());
            P.appendCloseTag(pp);
        }
        return P;
    }
    
    public void RunTests() throws TypeCheckException {
        Log.println("Tests starting");
        
        for(int i = 0; i < 4; i++) {
            Log.println("round " + i);
            Page P = RandomPage();
            
            for(int p = 'a'; p <= 'f'; p++)         // note that F results in the empty piecelist
                for(int q = 'a'; q <= 'f'; q++) {
                    Log.println("  cycle " + (char)p + " " + (char)q);
                    PieceSet x = P.getElem(String.valueOf((char)p));
                    PieceSet y = P.getElem(String.valueOf((char)q));
                    
                    Cycle(x, y);
                    
                    PieceSet r = PieceSet.OpUnion(x, y);
                    Cycle(x, r);
                    Cycle(y, r);
                    Cycle(r, x);
                    Cycle(r, y);
                }
        }
        
        Log.println("Tests completed");
    }
    
    void Cycle(PieceSet x, PieceSet y) throws TypeCheckException {  
        PieceSet A, B;
        
        A = PieceSet.OpMinus(x, y);
        B = NaiveOpMinus(x, y);
        Check("minus", x, y, A, B);
        
        A = PieceSet.OpIntersect(x, y, false);
        B = NaiveOpIntersect(x, y, false);
        Check("intersect", x, y, A, B);
        
        A = PieceSet.OpIntersect(x, y, true);
        B = NaiveOpIntersect(x, y, true);
        Check("not intersect", x, y, A, B);
        
        A = PieceSet.OpContain(x, y, false);
        B = NaiveOpContain(x, y, false);
        Check("contain", x, y, A, B);
        
        A = PieceSet.OpContain(x, y, true);
        B = NaiveOpContain(x, y, true);
        Check("not contain", x, y, A, B);
        
        A = PieceSet.OpDirectlyContain(x, y, false);
        B = NaiveOpDirectlyContain(x, y, false);
        Check("directly contain", x, y, A, B);
        
        A = PieceSet.OpDirectlyContain(x, y, true);
        B = NaiveOpDirectlyContain(x, y, true);
        Check("not directly contain", x, y, A, B);
        
        A = PieceSet.OpInside(x, y, false);
        B = NaiveOpInside(x, y, false);
        Check("in", x, y, A, B);
        
        A = PieceSet.OpInside(x, y, true);
        B = NaiveOpInside(x, y, true);
        Check("not in", x, y, A, B);
        
        A = PieceSet.OpDirectlyInside(x, y, false);
        B = NaiveOpDirectlyInside(x, y, false);
        Check("directly in", x, y, A, B);
        
        A = PieceSet.OpDirectlyInside(x, y, true);
        B = NaiveOpDirectlyInside(x, y, true);
        Check("not directly in", x, y, A, B);      
        
        A = PieceSet.OpOverlap(x, y, false);
        B = NaiveOpOverlap(x, y, false);
        Check("overlap", x, y, A, B);
        
        A = PieceSet.OpOverlap(x, y, true);
        B = NaiveOpOverlap(x, y, true);
        Check("not overlap", x, y, A, B);     
       
        A = PieceSet.OpBefore(x, y, false);
        B = NaiveOpBefore(x, y, false);
        Check("before", x, y, A, B);
        
        A = PieceSet.OpBefore(x, y, true);
        B = NaiveOpBefore(x, y, true);
        Check("not before", x, y, A, B);   
        
        A = PieceSet.OpAfter(x, y, false);
        B = NaiveOpAfter(x, y, false);
        Check("after", x, y, A, B);
        
        A = PieceSet.OpAfter(x, y, true);
        B = NaiveOpAfter(x, y, true);
        Check("not after", x, y, A, B);   
        
        A = PieceSet.OpDirectlyBefore(x, y, false);
        B = NaiveOpDirectlyBefore(x, y, false);
        Check("directly before", x, y, A, B);
        
        A = PieceSet.OpDirectlyBefore(x, y, true);
        B = NaiveOpDirectlyBefore(x, y, true);
        Check("not directly before", x, y, A, B);      
        
        A = PieceSet.OpDirectlyAfter(x, y, false);
        B = NaiveOpDirectlyAfter(x, y, false);
        Check("directly after", x, y, A, B);
        
        A = PieceSet.OpDirectlyAfter(x, y, true);
        B = NaiveOpDirectlyAfter(x, y, true);
        Check("not directly after", x, y, A, B); 
    }

    void Check(String op, PieceSet x, PieceSet y, PieceSet A, PieceSet B) throws TypeCheckException {
        if (!PieceSet.OpEqual(A, B)) {
            Log.println("");
            Log.println(op + " failed " + x.getSize() + " " + y.getSize());
            DumpPieceSet("x", x);
            DumpPieceSet("y", y);
            DumpPieceSet("A", A);
            DumpPieceSet("B", B);
        }
    }
    
    boolean Prob(double p) {
        return Math.random() < p;
    }
    
    String RandomName() {
        double x = Math.random();
        if (x < 0.2) return "a";
        else if (x < 0.4) return "b";
        else if (x < 0.6) return "c";
        else if (x < 0.8) return "d";
        else
            return "e";
    }
    
    void DumpPieceSet(String s, PieceSet x) {
        Log.print(s + ":");
        Cell p = x.head.next;
        
        while (p != x.head) {
            Log.print("[" + p.pce.beg.sno + "," + p.pce.end.sno + "]");
            p = p.next;
        }
        Log.println("");
    }
    
//
    // naive operator implementations
    //
    
    static PieceSet NaiveOpIntersect(PieceSet x, PieceSet y, boolean invert) {
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.next;
        while (p != x.head) {
            Cell q = y.head.next;
            boolean ok = false;
            while (q != y.head) {
                if (Piece.equal(p.pce, q.pce)) ok = true;
                q = q.next;
            }
            if (ok) {
                if (!invert) R.append(p.pce);
            } else if (invert)
                R.append(p.pce);
            p = p.next;
        }
        return R;
    }
    
    static PieceSet NaiveOpMinus(PieceSet x, PieceSet y) {
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.next;
        while (p != x.head) {
            Cell q = y.head.next;
            boolean ok = false;
            while (q != y.head) {
                if (Piece.equal(p.pce, q.pce)) ok = true;
                q = q.next;
            }
            if (!ok)
                R.append(p.pce);
            p = p.next;
        }
        return R;
    }
    
    static PieceSet NaiveOpContain(PieceSet x, PieceSet y, boolean invert) {
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.next;
        while (p != x.head) {
            Cell q = y.head.next;
            boolean ok = false;
            while (q != y.head) {
                if (Piece.contain(p.pce, q.pce)) ok = true;
                q = q.next;
            }
            if (ok) {
                if (!invert) R.append(p.pce);
            } else if (invert)
                R.append(p.pce);
            p = p.next;
        }
        return R;
    }
    
    static PieceSet NaiveOpInside(PieceSet x, PieceSet y, boolean invert) {
        
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.next;
        while (p != x.head) {
            Cell q = y.head.next;
            boolean ok = false;
            while (q != y.head) {
                if (Piece.in(p.pce, q.pce)) ok = true;
                q = q.next;
            }
            if (ok) {
                if (!invert) R.append(p.pce);
            } else if (invert)
                R.append(p.pce);            
            p = p.next;
        }
        return R;
    }
    
    static PieceSet NaiveOpOverlap(PieceSet x, PieceSet y, boolean invert) {
        
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.next;
        while (p != x.head) {
            Cell q = y.head.next;
            boolean ok = false;
            while (q != y.head) {
                if (Piece.overlap(p.pce, q.pce)) ok = true;
                q = q.next;
            }
            if (ok) {
                if (!invert) R.append(p.pce);
            } else if (invert)
                R.append(p.pce);            
            p = p.next;
        }
        return R;
    }
    
    static PieceSet NaiveOpBefore(PieceSet x, PieceSet y, boolean invert) {
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.next;
        while (p != x.head) {
            Cell q = y.head.next;
            boolean ok = false;
            while (q != y.head) {
                if (Piece.cbefore(p.pce, q.pce)) ok = true;
                q = q.next;
            }
            if (ok) {
                if (!invert) R.append(p.pce);
            } else if (invert)
                R.append(p.pce);            
            p = p.next;
        }
        return R;        
    }    
    
    static PieceSet NaiveOpAfter(PieceSet x, PieceSet y, boolean invert) {
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.next;
        while (p != x.head) {
            Cell q = y.head.next;
            boolean ok = false;
            while (q != y.head) {
                if (Piece.cafter(p.pce, q.pce)) ok = true;
                q = q.next;
            }
            if (ok) {
                if (!invert) R.append(p.pce);
            } else if (invert)
                R.append(p.pce);            
            p = p.next;
        }
        return R;           
    }   

    static PieceSet NaiveOpDirectlyInside(PieceSet x, PieceSet y, boolean invert) {
        PieceSet R = new PieceSet(x.page);
        
        PieceSet S = NaiveOpInside(x, y, invert);
        Cell p = S.head.next;
        while (p != S.head) {
            Cell q = S.head.next;
            boolean ok = true;
            while (q != S.head) {
                if (Piece.in(p.pce, q.pce)) ok = false;
                q = q.next;
            }
            if (ok) {
                if (!invert) R.append(p.pce);
            } else if (invert)
                R.append(p.pce);            
            p = p.next;
        }
        return R;
    }
    
    static PieceSet NaiveOpDirectlyContain(PieceSet x, PieceSet y, boolean invert) {
        PieceSet R = new PieceSet(x.page);
        
        PieceSet S = NaiveOpContain(x, y, invert);
        Cell p = S.head.next;
        while (p != S.head) {
            Cell q = S.head.next;
            boolean ok = true;
            while (q != S.head) {
                if (Piece.contain(p.pce, q.pce)) ok = false;
                q = q.next;
            }
            if (ok) {
                if (!invert) R.append(p.pce);
            } else if (invert)
                R.append(p.pce);            
            p = p.next;
        }
        return R;
    }    
    
    static PieceSet NaiveOpDirectlyBefore(PieceSet x, PieceSet y, boolean invert) {
        
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.next;
        while (p != x.head) {
            boolean ok = false;
            
            Cell q = y.head.next;            
            while (!ok && q != y.head) {
                if (Piece.cbefore(p.pce, q.pce)) {
                    ok = true;
                    Cell r = x.head.next;
                    while (r != x.head) {
                        if (Piece.cbefore(r.pce, q.pce) && Piece.endsafter(r.pce, p.pce)) ok = false;
                        r = r.next;
                    }
                }
                q = q.next;
            }
            if (ok) {
                if (!invert) R.append(p.pce);
            } else if (invert)
                R.append(p.pce); 
            p = p.next;
        }
        return R;
    }
    
    static PieceSet NaiveOpDirectlyAfter(PieceSet x, PieceSet y, boolean invert) {
        
        PieceSet R = new PieceSet(x.page);
        Cell p = x.head.next;
        while (p != x.head) {
            boolean ok = false;
            
            Cell q = y.head.next;
            while (!ok && q != y.head) {
                if (Piece.cafter(p.pce, q.pce)) {
                    ok = true;
                    Cell r = x.head.next;
                    while (r != x.head) {
                        if (r != p && Piece.inorder(r.pce, p.pce) && Piece.cafter(r.pce, q.pce)) ok = false;
                        r = r.next;
                    } 
                }
                q = q.next;
            }
            if (ok) {
                if (!invert) R.append(p.pce);
            } else if (invert)
                R.append(p.pce);            
            p = p.next;
        }
        return R;
    }    
    
}

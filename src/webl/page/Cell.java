package webl.page;

public class Cell
{
    Piece   pce;            // the piece itself
    Cell    prev, next;     // prevous and next pointers

    public Cell(Piece p) {
        this.pce = p;
        prev = next = this;
    }
}
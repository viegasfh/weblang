/*
    
////////////// Known issues //////////////////

IMPORTANT ISSUES

* stdin parameter for Call and Exec
* NewNamedPiece(name, pieceset): pieceset ?
* correct handling of lexical scopes in ExecArgs and CallArgs
* Seq(piece, seqexpr): pieceset ?
* CDATA pieces in XML are not documented yet.
* Parsing of XML DOCTYPE (or PI?) in the more common attribute list fashion
* Seems to be a problem with MacroBufReader reading the HTML 4.0 DTD,
    in the sense that sometimes a single character gets lost. Remove the
    first space before the first occurrence of "that" in the DTD to make
    the problem appear. This causes the > of the CENTER element to disappear.
* How about: Sort(s: set):list  ?
* correct handling of temporary files
* is tag comparison implemented ?
* comment the source code / code review
* parsing of doctype attributes
* Checking for JDK 1.1 @ startup

TWEAKS & IDEAS

* optimize emition of piece attribute values in class Piece
* println, print using different encodings?
* static checking if argument counts match?
* also support push(char[]) in MacroBufReader
* improve parser structure with "expect" method
* incorrect parsing in the style of (x, y, z,) 
* replace PriorityQueue with something decent
* variable override warning

EVENTUAL WORK
* more tricks regarding URL resolution
* pick a better HTTP implementation
* test suite
* support variable length arguments
* sugar "method" and "function"
* further handling of character entities

*/

class Notebook
{
}


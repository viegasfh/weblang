package webl.page.json;

import com.geekstakulus.json.node.*;
import com.geekstakulus.json.analysis.*;
import webl.page.*;
import java.util.*;
import webl.page.*;
import webl.page.net.*;
import webl.lang.expr.*;

public class PageBuilder extends DepthFirstAdapter {
  Page page = new Page(null, Page.JSON);
  Deque<Piece> stack = new LinkedList<Piece>();

  public PageBuilder() {
  }

  public Page getPage() {
    return this.page;
  }

  // opens the json tag
  public void inAJson(AJson node) {
    Piece piece = page.appendOpenTag("json");
    stack.push(piece);
  }

  // closes the json tag
  public void outAJson(AJson node) {
    Piece piece = stack.pop(); 
    page.appendCloseTag(piece);
  }

  // sets the type attribute of the tag to object
  public void inAObjectValue(AObjectValue node) {
    Piece piece = stack.peek();

    piece.setAttr("type", "object");
  }

  // sets the type attribute of the tag to array
  public void inAArrayValue(AArrayValue node) {
    Piece piece = stack.peek();
    piece.setAttr("type", "array");
  }

  // sets the type attribute to number
  // and sets the tag value to the number
  public void outANumberValue(ANumberValue node) {
    String value = node.getNumber().getText();

    Piece piece = stack.peek();
    piece.setAttr("type", "number");
    page.appendPCData(value);
  }

 // sets the tag attribute to string
 // and sets its value to the value of the string
 public void outAStringValue(AStringValue node) {
    String value = node.getString().getText();

    Piece piece = stack.peek();
    piece.setAttr("type", "string");
    page.appendPCData(value.substring(1, value.length()-1));
  }

  // sets the tag attribute to boolean
  // and sets its value to true
  public void caseATrueValue(ATrueValue node) {
      Piece piece = stack.peek();
      piece.setAttr("type", "boolean");
      page.appendPCData("true");
  }

  // sets the tag attribute to boolean
  // and sets its value to false
  public void caseAFalseValue(AFalseValue node) {
      Piece piece = stack.peek();
      piece.setAttr("type", "boolean");
      page.appendPCData("false");
  }

  // generates the null pcdata
  // sets the tag attribute type to undefined
  public void caseANullValue(ANullValue node) {
      Piece piece = stack.peek();
      piece.setAttr("type", "undefined");
      page.appendPCData("null");
  }

  // creates the tags for the object fields
  public void inAPairMember(APairMember node) {
    String stringName = node.getString().getText();
    String nodeName = stringName.substring(1, stringName.length()-1);
    Piece piece = page.appendOpenTag(nodeName); // open the tag
    stack.push(piece);
  }

  // closes the tags for the object fields
  public void outAPairMember(APairMember node) {
    Piece piece = stack.pop();
    page.appendCloseTag(piece); // close the tag
  }

  // create an open tag for element
  public void inAValueElement(AValueElement node) {
    Piece piece = page.appendOpenTag("element");
    stack.push(piece);
  }

  // closes the tag element
  public void outAValueElement(AValueElement node) {
    Piece piece = stack.pop();
    page.appendCloseTag(piece);
  }
}
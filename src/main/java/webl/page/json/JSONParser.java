package webl.page.json;

import java.io.*;
import java.util.*;
import webl.page.*;
import webl.page.net.*;
import webl.lang.expr.*;
import webl.util.*;
import javax.json.Json;
import javax.json.stream.*;

public class JSONParser implements ParserInterface {
  private String      documenturl;
  
  public JSONParser() {
  }

  public String DefaultCharset() {
      return "UTF8";
  }

  public Page Parse(Reader TR, String url, ObjectExpr options) throws IOException {
    documenturl = url;
    Page page = new Page(null, Page.JSON);
    Deque<Piece> stack = new LinkedList<Piece>();

    // Create the initial piece
    Piece piece = page.appendOpenTag("json");
    stack.push(piece);

    try {
      JsonParser parser = Json.createParser(new BufferedReader(TR));

      while (parser.hasNext()) {
        JsonParser.Event event = parser.next();

        if (event == JsonParser.Event.START_ARRAY) {
          piece = stack.peek();
          String typeAttr = piece.getAttr("type");

          if (typeAttr == null) {
            piece.setAttr("type", "array");
          } else if (typeAttr.equals("array")) {
            piece = page.appendOpenTag("element");
            piece.setAttr("type", "array");
            stack.push(piece);
          }
          
        } else if (event == JsonParser.Event.END_ARRAY || event == JsonParser.Event.END_OBJECT) {
          piece = stack.pop();
          page.appendCloseTag(piece); // close the tag
        } else if (event == JsonParser.Event.START_OBJECT) {
          piece = stack.peek();
          String typeAttr = piece.getAttr("type");

          if (typeAttr == null) {
            piece.setAttr("type", "object");
          } else if (typeAttr.equals("array")) {
            piece = page.appendOpenTag("element");
            piece.setAttr("type", "object");
            stack.push(piece);
          }
        } else if (event == JsonParser.Event.VALUE_FALSE || event == JsonParser.Event.VALUE_TRUE) {
          piece = stack.peek();
          String typeAttr = piece.getAttr("type");

          if (typeAttr == null) {
            piece = stack.pop();
            piece.setAttr("type", "boolean");
          } else if (typeAttr.equals("array")) {
            piece = page.appendOpenTag("element");
            piece.setAttr("type", "boolean");
          }
          page.appendPCData(event.toString().equals("VALUE_FALSE") ? "false" : "true");
          page.appendCloseTag(piece);
        } else if (event == JsonParser.Event.VALUE_NULL) {
          piece = stack.peek();
          String typeAttr = piece.getAttr("type");

          if (typeAttr == null) {
            piece = stack.pop();
            piece.setAttr("type", "undefined");
          } else if (typeAttr.equals("array")) {
            piece = page.appendOpenTag("element");
            piece.setAttr("type", "undefined");
          }
          page.appendPCData("null");
          page.appendCloseTag(piece);
        } else if (event == JsonParser.Event.VALUE_STRING) {
          piece = stack.peek();
          String typeAttr = piece.getAttr("type");

          if (typeAttr == null) {
            piece = stack.pop();
            piece.setAttr("type", "string");
          } else if (typeAttr.equals("array")) {
            piece = page.appendOpenTag("element");
            piece.setAttr("type", "string");
          }
          page.appendPCData(parser.getString());
          page.appendCloseTag(piece);
        } else if (event == JsonParser.Event.VALUE_NUMBER) {
          piece = stack.peek();
          String typeAttr = piece.getAttr("type");

          if (typeAttr == null) {
            piece = stack.pop();
            piece.setAttr("type", "number");
          } else if (typeAttr.equals("array")) {
            piece = page.appendOpenTag("element");
            piece.setAttr("type", "number");
          }
          page.appendPCData(parser.getString());
          page.appendCloseTag(piece);
        } else if (event == JsonParser.Event.KEY_NAME) {
          piece = page.appendOpenTag(parser.getString()); // open the tag
          stack.push(piece);
        }
      }
    } catch (Exception jpex) {
      Log.debugln(jpex.getMessage() + " (" + documenturl + ")]");
      throw new IOException(jpex.getMessage());
    } 

    return page; 
  }
}
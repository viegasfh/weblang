package webl.page.json;

import com.geekstakulus.json.node.*;
import com.geekstakulus.json.parser.*;
import com.geekstakulus.json.lexer.*;
import java.io.*;
import java.util.*;
import webl.page.*;
import webl.page.net.*;
import webl.lang.expr.*;

import webl.util.*;

public class JSONParser implements ParserInterface {
  private String      documenturl;
  
  public JSONParser() {
  }

  public String DefaultCharset() {
      return "UTF8";
  }
  
  public Page Parse(Reader TR, String url, ObjectExpr options) throws IOException {
    documenturl = url;
    PageBuilder pageBuilder = new PageBuilder();

    try {
      Lexer lexer = new Lexer(new PushbackReader(new BufferedReader(TR)));
      Parser parser = new Parser(lexer);
      Start start = parser.parse();
      
      start.apply(pageBuilder);
    } catch (Exception ex) {
      Log.debugln(ex.getMessage() + " (" + documenturl + ")]");
    }

    return pageBuilder.getPage();
  }
}
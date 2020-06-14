package weblx.files;

import webl.lang.*;
import webl.lang.expr.*;
import webl.lang.builtins.*;
import webl.page.*;
import webl.page.net.*;

import java.io.*;
import java.util.*;

public class ListFun extends AbstractFunExpr
{
  public String toString() {
    return "<Files_List>";
  }

  public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
    CheckArgCount(c, args, callsite, 1);
    String dirname = StringArg(c, args, callsite, 0);

    File f = new File(dirname);
    String[] content = f.list();

    ListExpr li = new ListExpr();
    for (int i=0; i < content.length; i++)
      li = li.Append(Program.Str(content[i]));

    return li;
  }
}

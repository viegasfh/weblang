package webl.page.net;

import webl.page.*;
import webl.lang.expr.*;
import java.io.*;

public interface ParserInterface
{
    public Page Parse(Reader R, String url, ObjectExpr options) throws IOException;
    public String DefaultCharset();
}


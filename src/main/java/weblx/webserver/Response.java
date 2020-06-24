package weblx.webserver;

import webl.lang.*;
import webl.lang.expr.*;
import webl.util.*;

import java.util.*;
import java.io.*;

public class Response extends ObjectExpr
{
    private File file = null;

    public Response(int statuscode, String msg) {
        setStatus(statuscode, msg);
        def("header", new ObjectExpr());
        def("result", Program.nilval);
        setHeader("Server", "WebL");
        setHeader("Content-Type", "text/html");
        setHeader("Date", new Date().toString());
    }

    private String getStr(String key) {
        Expr v = get(key);
        if (v != null && v != Program.nilval)
            return v.print();
        else
            return "";
    }

    public void setStatus(int statuscode, String msg) {
        def("statuscode", Program.Int(statuscode));
        def("statusmsg", Program.Str(msg));
    }

    public void setResult(String result) {
        file = null;
        def("result", Program.Str(result));
    }

    public void setResult(File F) {
        this.file = F;
    }

    public void setHeader(String name, String value) {
        ObjectExpr header = (ObjectExpr)get("header");
        header.def(name, Program.Str(value));
    }

    /** Write the complete response, headers and all to the output stream */
    public void write(DataOutputStream out) throws IOException {
        out.writeBytes("HTTP/1.0 " + getStr("statuscode") + " " + getStr("statusmsg") + "\r\n");

        if (file == null) {
            String result = getStr("result");
            if (result.equals("")) {
                result = "<html><body><b>" + getStr("statuscode") + "</b> "
                        + getStr("statusmsg") + "</body></html>";
            }
            setHeader("Content-Length", String.valueOf(result.length()));

            writeHeaders(out);
            out.writeBytes("\r\n");
            out.writeBytes(result);
        } else {
            setHeader("Content-Length", String.valueOf(file.length()));
            setHeader("Last-Modified", new Date(file.lastModified()).toString());
            setHeader("Content-Type", getContentType(file));

            writeHeaders(out);
            out.writeBytes("\r\n");

            InputStream is = null;
            try {
                is = new FileInputStream(file.getAbsolutePath());
                byte[] buf = new byte[2048];
                int n;
                while ((n = is.read(buf)) > 0) {
                    out.write(buf, 0, n);
                }
            } catch (FileNotFoundException e) {
            } finally {
                if(is != null) is.close();
            }
        }
    }

    private void writeHeaders(DataOutputStream out) throws IOException {
        ObjectExpr header = (ObjectExpr)get("header");
        Enumeration e = header.EnumKeys();
        while(e.hasMoreElements()) {
            Expr n = (Expr)e.nextElement();
            Expr v = header.get(n);
            if (v instanceof ListExpr) {
                Enumeration ee = ((ListExpr)v).getContent();
                while (ee.hasMoreElements()) {
                    out.writeBytes(n.print());
                    out.writeBytes(": ");
                    Expr vv = (Expr)ee.nextElement();
                    out.writeBytes(vv.print());
                    out.writeBytes("\r\n");
                }
            } else {
                out.writeBytes(n.print());
                out.writeBytes(": ");
                out.writeBytes(v.print());
                out.writeBytes("\r\n");
            }
        }
    }

    // Content type stuff

    static Hashtable<String, String> map = new Hashtable<String, String>();

    String getContentType(File file) {
        String name = file.getName();
        int p = name.lastIndexOf('.');
        String typ = null;
        if (p > 0)
            typ = map.get(name.substring(p));
        if (typ == null)
            return "content/unknown";
        else
            return typ;
    }

    static {
        map.put("", "content/unknown");
        map.put(".uu", "application/octet-stream");
        map.put(".exe", "application/octet-stream");
        map.put(".ps", "application/postscript");
        map.put(".zip", "application/zip");
        map.put(".sh", "application/x-shar");
        map.put(".tar", "application/x-tar");
        map.put(".snd", "audio/basic");
        map.put(".au", "audio/basic");
        map.put(".wav", "audio/x-wav");
        map.put(".gif", "image/gif");
        map.put(".jpg", "image/jpeg");
        map.put(".jpeg", "image/jpeg");
        map.put(".htm", "text/html");
        map.put(".html", "text/html");
        map.put(".text", "text/plain");
        map.put(".c", "text/plain");
        map.put(".cc", "text/plain");
        map.put(".c++", "text/plain");
        map.put(".h", "text/plain");
        map.put(".pl", "text/plain");
        map.put(".txt", "text/plain");
        map.put(".java", "text/plain");
        map.put(".webl", "text/plain");
        map.put(".xml", "text/xml");
    }
}

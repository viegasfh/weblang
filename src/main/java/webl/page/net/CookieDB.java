package webl.page.net;

import java.net.*;
import java.util.*;
import java.io.*;
import webl.util.Log;
import weblx.url.MalformedURL;

public class CookieDB
{
    protected Hashtable hash = new Hashtable();

    public synchronized void SaveCookie(URL url, String cookie) {
        if (cookie != null) {
            try {
                Cookie C = new Cookie(url, cookie);

                // do a sanity check
                if (!C.Match(url)) {
                    // Host is trying to set an illegal cookie
                    Log.debugln("[Host " + url.getHost() + " attempting to set a cookie on the wrong domain: " + cookie + "]");
                    return;
                }
                SaveCookie(C);
            } catch (IllegalCookieException e) {
                // just ignore the bad cookie
                Log.debugln("[Bad cookie: " + e + "; header=" + cookie + "]");
            } catch (MalformedURL e) {
                // just ignore the bad cookie
                Log.debugln("[Bad URL in cookie: " + e + "; header=" + cookie + "]");
            }
        }
    }

    public synchronized void SaveCookie(Cookie C) {
        if (C != null) {
            Cookie first = (Cookie)hash.get(C.domainkey);

            if (first == null) {
                hash.put(C.domainkey, C);
            } else {        // add entry
                Cookie p = first;
                Cookie prev = null;

                // we keep the entries sorted by path lengths
                while(p != null && p.pathlen >= C.pathlen) {
                    if (C.domain.equals(p.domain) && C.cookiename.equals(p.cookiename)
                        && C.path.equals(p.path)) {     // replace p with C
                        C.next = p.next;
                        if (prev != null)
                            prev.next = C;
                        else
                            hash.put(C.domainkey, C);
                        return;
                    }
                    prev = p;
                    p = p.next;
                }
                // put in between prev and p
                C.next = p;
                if (prev != null)
                    prev.next = C;
                else
                    hash.put(C.domainkey, C);
            }
        }
    }

// returns null if no cookies are available in DB
    private synchronized String GetCookies(URL url) {
        StringBuffer s = new StringBuffer();
        String hashkey = Cookie.HashKey(url.getHost());
        Cookie p = (Cookie)hash.get(hashkey);
        while (p != null) {
            if (p.Match(url)) {
                if (s.length() > 0)
                    s.append("; ");
                s.append(p.cookiename).append('=').append(p.cookieval);
            }
            p = p.next;
        }
        if (s.length() > 0)
            return s.toString();
        else
            return null;
    }

    public synchronized void SetCookie(URL url, HttpURLConnection c) {
        String cookie = GetCookies(url);
        if (cookie != null) {
            c.setRequestProperty("Cookie", cookie);
        }
    }

    public synchronized void Load(Reader data) throws IOException, IllegalCookieException, MalformedURL {
        BufferedReader R = new BufferedReader(data);
        String line = R.readLine();
        while (line != null) {
            Cookie C = new Cookie(line);
            SaveCookie(C);
            line = R.readLine();
        }
    }

    public synchronized void Save(Writer data) throws IOException  {
        PrintWriter W = new PrintWriter(data);
        Enumeration enumeration = hash.elements();
        while(enumeration.hasMoreElements()) {
            Cookie C = (Cookie)enumeration.nextElement();
            while (C != null) {
                W.println(C.toString());
                C = C.next;
            }
        }
    }
}


package webl.util;

import java.io.*;
import java.util.*;

public class FileLocator
{
    private static Vector<String> dirs = new Vector<String>();
    private static boolean loaded = false;

    static private File FindFile(String name) {
        if (!loaded)
            LoadDirs();

        File f = new File(name);
        if (f.canRead())
            return f;

        if (dirs != null) {
            for (int i = 0; i < dirs.size(); i++) {
                f = new File((String)dirs.elementAt(i), name);
                if (f.canRead())
                    return f;
            }
        }
        return null;
    }

    static public InputStream Find(String filename) {
        try {
            File f = FindFile(filename);
            if (f != null)
                return new FileInputStream(f);
            else
                return Class.forName("webl.util.FileLocator")
                    .getResourceAsStream("/scripts/" + filename);
        } catch (FileNotFoundException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    // lastmodified should be an array of length 1.
    static public InputStream Find(String filename, long[] lastmodified) {
        try {
            lastmodified[0] = 0;
            File f = FindFile(filename);
            if (f != null) {
                lastmodified[0] = f.lastModified();
                return new FileInputStream(f);
            } else
                return Class.forName("webl.util.FileLocator")
                    .getResourceAsStream("/scripts/" + filename);
        } catch (FileNotFoundException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    static public void ClearDirs(String path) {
        dirs = new Vector<String>();
        loaded = false;
    }

    static public void AddSearchDirs(String path) {
        StringTokenizer st = new StringTokenizer(path, System.getProperty("path.separator"), false);
        while (st.hasMoreTokens()) {
            String dir = st.nextToken();
            dirs.addElement(dir);
        }
        loaded = true;
    }

    /* this is not a static initializer because the webl.properties file might not have been loaded yet */
    static void LoadDirs() {
        Properties p = System.getProperties();
        String weblpath = p.getProperty("webl.path");
        if (weblpath == null)
            AddSearchDirs(".");
        else
            AddSearchDirs(weblpath);
    }
}

package webl.lang;

import java.util.*;

// interface the every statement uses to extract the contents of an set/list/piecelist-like object
public interface ContentEnumeration
{
    public Enumeration getContent();
}
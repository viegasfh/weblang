package weblx.str;

import webl.lang.*;
import webl.lang.expr.*;
import webl.lang.builtins.*;
import java.util.*;
import java.util.regex.*;

public class SearchFun extends AbstractFunExpr
{
    public String toString() {
        return "<Search>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);

        String s = StringArg(c, args, callsite, 0);
        String r = StringArg(c, args, callsite, 1);
        
        Pattern pattern;
        try {
            pattern = Pattern.compile(r);
        } catch(PatternSyntaxException e) {
            throw new WebLException(c, callsite, "ArgumentError", "malformed regular expression pattern");
        }
        
        ListExpr    res = new ListExpr();
        
        Matcher matcher = pattern.matcher(s);

        while (matcher.find()) {
            ObjectExpr obj = new ObjectExpr();
            
            int groups = matcher.groupCount();
            
            for(int group = 0; group < groups; group++) {
                String g = matcher.group(group);
                if (g == null) g = "";
                obj.def(Program.Int(group), Program.Str(g));
            }
            res = res.Append(obj);
        }
        return res;
    }
}

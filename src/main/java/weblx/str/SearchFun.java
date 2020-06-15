package weblx.str;

import webl.lang.*;
import webl.lang.expr.*;
import webl.lang.builtins.*;
import java.util.*;
import com.oroinc.text.regex.*;

public class SearchFun extends AbstractFunExpr
{
    public String toString() {
        return "<Search>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        CheckArgCount(c, args, callsite, 2);

        String s = StringArg(c, args, callsite, 0);
        String r = StringArg(c, args, callsite, 1);
        
        PatternCompiler compiler = new Perl5Compiler();
        Pattern pattern;
        try {
            pattern = compiler.compile(r);
        } catch(MalformedPatternException e) {
            throw new WebLException(c, callsite, "ArgumentError", "malformed regular expression pattern");
        }
        
        ListExpr    res = new ListExpr();
        
        PatternMatcherInput input = new PatternMatcherInput(s);
        PatternMatcher matcher = new Perl5Matcher();
        while (matcher.contains(input, pattern)) {
            ObjectExpr obj = new ObjectExpr();
            
            MatchResult result = matcher.getMatch();
            int groups = result.groups();
            
            for(int group = 0; group < groups; group++) {
                String g = result.group(group);
                if (g == null) g = "";
                obj.def(Program.Int(group), Program.Str(g));
            }
            res = res.Append(obj);
        }
        return res;
    }
}

package weblx.str;

import webl.lang.*;
import webl.lang.expr.*;
import webl.lang.builtins.*;
import java.util.*;
import com.oroinc.text.regex.*;

public class EndsWithFun extends AbstractFunExpr
{
    public String toString() {
        return "<EndsWith>";
    }
    
    public Expr Apply(Context c, Vector args, Expr callsite) throws WebLException {
        if (args.size() != 2)
            throw new WebLException(c, callsite, "ArgumentError", "wrong number of arguments passed to match function");
            
        Expr s = ((Expr)(args.elementAt(0))).eval(c);
        if (!(s instanceof StringExpr))
            throw new WebLException(c, callsite, "ArgumentError", "match function expects a string as first argument");

        Expr r = ((Expr)(args.elementAt(1))).eval(c);
        if (!(r instanceof StringExpr))
            throw new WebLException(c, callsite, "ArgumentError", "match function expects a regular expression string as second argument");
        

        PatternCompiler compiler = new Perl5Compiler();
        Pattern pattern;
        try {
            pattern = compiler.compile(((StringExpr)r).val());
        } catch(MalformedPatternException e) {
            throw new WebLException(c, callsite, "ArgumentError", "malformed regular expression pattern");
        }
        
        PatternMatcher matcher = new Perl5Matcher();
        PatternMatcherInput input = new PatternMatcherInput(((StringExpr)s).val());
        
        while (matcher.contains(input, pattern)) {
            MatchResult result = matcher.getMatch();
            if (result.endOffset(0) == ((StringExpr)s).val().length())
                return Program.trueval;
        }
        return Program.falseval;
    }
}
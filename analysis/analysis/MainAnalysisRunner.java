package analysis;

import petter.cfg.CompilationUnit;
import petter.cfg.DotLayout;
import petter.cfg.Procedure;
import petter.cfg.expression.Expression;
import petter.cfg.expression.Variable;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class MainAnalysisRunner {
    public static void main(String[] args) throws Exception {
        String filePath = "/Users/denis.g/Developer/simpleC-ssa/analysis/TrueLivenessFiles/" + "test.c";
        CompilationUnit cu = petter.simplec.Compiler.parse(new File(filePath));

        TrueLivenessAnalysis la = new TrueLivenessAnalysis(cu);
        Procedure main = cu.getProcedure("main");
        DotLayout layout = new DotLayout("jpg","main.jpg");

        Set<Expression> liveExprs = new HashSet<Expression>();
        Variable returnVar = new Variable(0, "return", null);
        liveExprs.add(returnVar);

        la.enter(main, liveExprs);
        la.fullAnalysis();

        main.getStates().forEach(s -> layout.highlight(s, la.annotationRepresentationOfState(s)));

        layout.callDot(main);
    }
}

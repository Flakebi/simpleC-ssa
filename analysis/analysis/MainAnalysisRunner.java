package analysis;

import petter.cfg.CompilationUnit;
import petter.cfg.DotLayout;
import petter.cfg.Procedure;
import petter.cfg.expression.Expression;
import petter.cfg.expression.Variable;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MainAnalysisRunner {
    public static void main(String[] args) throws Exception {
        String filePath = "/Users/denis.g/Developer/simpleC-ssa/analysis/RegisterAllocationFiles/" + "test2.c";
        CompilationUnit cu = petter.simplec.Compiler.parse(new File(filePath));

        TrueLivenessAnalysis la = new TrueLivenessAnalysis(cu);
        RegisterAllocationAnalysis ra = new RegisterAllocationAnalysis(cu, la);

        Procedure main = cu.getProcedure("main");

        Set<Variable> liveExprs = new HashSet<Variable>();
        Variable returnVar = new Variable(0, "return", null);
        liveExprs.add(returnVar);

        la.enter(main, liveExprs);
        la.fullAnalysis();

        ra.enter(main, new HashMap<>());
        ra.fullAnalysis();

        DotLayout layout = new DotLayout("jpg","main.jpg");
        main.getStates().forEach(s -> layout.highlight(s, ra.annotationRepresentationOfState(s)));

        layout.callDot(main);
    }
}

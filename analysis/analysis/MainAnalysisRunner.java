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
import petter.utils.Tupel;

public class MainAnalysisRunner {
    public static void main(String[] args) throws Exception {
        String filePath = "/Users/Laci/Documents/Uni/progOpt/simpleC-ssa/analysis/RegisterAllocationFiles/" + "test2.c";
        CompilationUnit cu = petter.simplec.Compiler.parse(new File(filePath));

        ReachingDefinitionsAnalysis rda = new ReachingDefinitionsAnalysis(cu);
        TrueLivenessAnalysis la = new TrueLivenessAnalysis(cu);
        RegisterAllocationAnalysis ra = new RegisterAllocationAnalysis(cu, la);

        Procedure main = cu.getProcedure("main");

        rda.enter(main, new HashSet<Tupel<Integer, Long>>());
        rda.fullAnalysis();

        Set<Variable> liveExprs = new HashSet<Variable>();
        Variable returnVar = new Variable(0, "return", null);
        liveExprs.add(returnVar);

        la.enter(main, liveExprs);
        la.fullAnalysis();

        ra.enter(main, new HashMap<>());
        ra.fullAnalysis();

        DotLayout layout = new DotLayout("jpg","main.jpg");
        main.getStates().forEach(s -> layout.highlight(s, rda.annotateRepresentationOfState(s)));
        //main.getStates().forEach(s -> layout.highlight(s, ra.annotationRepresentationOfState(s)));

        layout.callDot(main);
    }
}

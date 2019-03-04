package analysis;

import analysis.constfolding.ConstFolding;
import petter.cfg.CompilationUnit;
import petter.cfg.DotLayout;
import petter.cfg.Procedure;
import petter.cfg.State;
import petter.cfg.edges.Transition;
import petter.cfg.expression.Expression;
import petter.cfg.expression.Variable;

import java.io.File;
import java.util.*;

import petter.cfg.expression.types.Char;
import petter.utils.Tupel;

public class MainAnalysisRunner {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Please provide the path to a test file as argument");
            return;
        }
        String filePath = args[0];
        CompilationUnit cu = petter.simplec.Compiler.parse(new File(filePath));
        Procedure main = cu.getProcedure("main");
        SSATransform transform = new SSATransform();
        transform.procedure = main;

        transform.processJoins();
        // reaching definitions
        ReachingDefinitionsAnalysis rda = new ReachingDefinitionsAnalysis(cu);

        rda.enter(main, new HashSet<>());
        rda.fullAnalysis();
        transform.insertAssignments(rda);

        ReachingDefinitionsAnalysis rda1 = new ReachingDefinitionsAnalysis(cu);

        rda1.enter(main, new HashSet<>());
        rda1.fullAnalysis();

        transform.replaceVars(rda1);

        DotLayout layout = new DotLayout("jpg","results/reaching_definitions.jpg");
        main.getStates().forEach(s -> layout.highlight(s, rda1.annotateRepresentationOfState(s)));
        layout.callDot(main);

        // constant propagation
        final var cf = new ConstFolding();
        cf.fold(main);

        DotLayout layoutCf = new DotLayout("jpg","results/constfolding.jpg");
        main.getStates().forEach(s -> layoutCf.highlight(s, cf.getValues(s).toString()));
        layoutCf.callDot(main);

        // true liveness
        TrueLivenessAnalysis la = new TrueLivenessAnalysis(cu);
        Set<Variable> liveExprs = new HashSet<>();
        Variable returnVar = new Variable(0, "return", null);
        liveExprs.add(returnVar);

        la.enter(main, liveExprs);
        la.fullAnalysis();

        DotLayout layoutLA = new DotLayout("jpg","results/true_liveness.jpg");
        main.getStates().forEach(s -> layoutLA.highlight(s, la.annotationRepresentationOfState(s)));
        layoutLA.callDot(main);


        // register allocation
        RegisterAllocationAnalysis ra = new RegisterAllocationAnalysis(cu, la);
        ra.enter(main, new HashMap<>());
        ra.fullAnalysis();

        DotLayout layoutRA = new DotLayout("jpg","results/register_allocation.jpg");
        main.getStates().forEach(s -> layoutRA.highlight(s, ra.annotationRepresentationOfState(s)));
        layoutRA.callDot(main);
    }
}

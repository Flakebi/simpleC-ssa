package analysis;

import java.io.File;
import java.util.*;

import petter.cfg.CompilationUnit;
import petter.cfg.DotLayout;
import petter.cfg.Procedure;
import petter.cfg.State;
import petter.cfg.edges.Transition;
import petter.cfg.expression.Expression;
import petter.cfg.expression.Variable;
import petter.cfg.expression.types.Char;
import petter.utils.Tupel;

import analysis.constfolding.ConstFolding;
import analysis.redundancyelimination.*;

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

        DotLayout layout = new DotLayout("png","results/reaching_definitions.png");
        main.getStates().forEach(s -> layout.highlight(s, rda1.annotateRepresentationOfState(s)));
        layout.callDot(main);

        // constant propagation
        final var cf = new ConstFolding();
        cf.fold(main);

        DotLayout layoutCf = new DotLayout("png","results/constfolding.png");
        main.getStates().forEach(s -> layoutCf.highlight(s, cf.getValues(s).toString()));
        layoutCf.callDot(main);


        // Partially Redundant Code
        AvailableExpressionAnalysis availExprs = new AvailableExpressionAnalysis();
        VeryBusyExpressionAnalysis busyExprs = new VeryBusyExpressionAnalysis();

        Procedure p = main;
        availExprs.enter(p, new HashSet<>());
        availExprs.fullAnalysis();
        busyExprs.enter(p, new HashSet<>());
        busyExprs.fullAnalysis();

        RedundancyEliminationVisitor1 vis1 = new RedundancyEliminationVisitor1(p, availExprs, busyExprs);
        vis1.enter(p);
        vis1.fullAnalysis();
        p.refreshStates();
        RedundancyEliminationVisitor2 vis2 = new RedundancyEliminationVisitor2(vis1);
        vis2.enter(p);
        vis2.fullAnalysis();

        DotLayout layoutRed = new DotLayout("png", "results/redundancyelimination.png");
        main.getStates().forEach(s -> {
            var avail = availExprs.dataflowOf(s);
            if (avail != null)
                layoutRed.highlight(s,avail + " ;; " + busyExprs.dataflowOf(s));
        });
        layoutRed.callDot(p);


        // true liveness
        TrueLivenessAnalysis la = new TrueLivenessAnalysis(cu);
        Set<Variable> liveExprs = new HashSet<>();
        Variable returnVar = new Variable(0, "return", null);
        liveExprs.add(returnVar);

        la.enter(main, liveExprs);
        la.fullAnalysis();

        DotLayout layoutLA = new DotLayout("png","results/true_liveness.png");
        main.getStates().forEach(s -> layoutLA.highlight(s, la.annotationRepresentationOfState(s)));
        layoutLA.callDot(main);


        // register allocation
        RegisterAllocationAnalysis ra = new RegisterAllocationAnalysis(cu, la);
        ra.enter(main, new HashMap<>());
        ra.fullAnalysis();

        DotLayout layoutRA = new DotLayout("png","results/register_allocation.png");
        main.getStates().forEach(s -> layoutRA.highlight(s, ra.annotationRepresentationOfState(s)));
        layoutRA.callDot(main);
    }
}

package analysis;

import petter.cfg.*;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.edges.Transition;
import petter.cfg.expression.Expression;
import petter.cfg.expression.Variable;

import java.io.File;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RegisterAllocationAnalysis extends AbstractPropagatingVisitor<Map<Variable, Integer>> {
    CompilationUnit cu;
    TrueLivenessAnalysis la;

    public RegisterAllocationAnalysis(CompilationUnit cu, TrueLivenessAnalysis la) {
        super(true);
        this.cu = cu;
        this.la = la;
    }

    public Map<Variable, Integer> visit(ProcedureCall procedureCall, Map<Variable, Integer> objectObjectMap) {
        return null;
    }

    public Map<Variable, Integer> visit(State s, Map<Variable, Integer> parentFlow) {
        Map<Variable, Integer> oldFlow = dataflowOf(s);
        Map<Variable, Integer> newFlow = new HashMap<>();

        Set<Integer> registersBusyFromParent = parentFlow.entrySet()
                .stream()
                .filter(e -> la.dataflowOf(s).contains(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());

        for (Variable var : la.dataflowOf(s)) {
            if (parentFlow.containsKey(var)) {
                newFlow.put(var, parentFlow.get(var));
            } else {
                int firstAvailableReg = IntStream
                        .iterate(0, i -> i + 1)
                        .filter(i -> !newFlow.values().contains(i) && !registersBusyFromParent.contains(i))
                        .findFirst()
                        .getAsInt();

                newFlow.put(var, firstAvailableReg);
            }
        }

        if (oldFlow != null && oldFlow.equals(newFlow)) {
            return null;
        }

        dataflowOf(s, newFlow);
        return newFlow;
    }

    String annotationRepresentationOfState(State s) {
        return dataflowOf(s)
                .entrySet()
                .stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .map(e -> "R" + e.getValue() + " = " + e.getKey().toString())
                .collect(Collectors.joining("|"));
    }
}

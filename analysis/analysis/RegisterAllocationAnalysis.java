package analysis;

import petter.cfg.AbstractPropagatingVisitor;
import petter.cfg.CompilationUnit;
import petter.cfg.State;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.expression.Variable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public Map<Variable, Integer> visit(State state, Map<Variable, Integer> parentFlow) {
        Set<Integer> registersBusyFromParent = parentFlow.entrySet()
                .stream()
                .filter(e -> la.dataflowOf(state).contains(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());

        Set<Variable> liveVars = la.dataflowOf(state);
        Map<Variable, Integer> oldFlow = dataflowOf(state);
        Map<Variable, Integer> newFlow = liveVars
                .stream()
                .reduce(new HashMap<>(), (acc, var) -> {
                    int firstAvailableReg = IntStream
                            .iterate(0, i -> i + 1)
                            .filter(i -> !acc.values().contains(i) && !registersBusyFromParent.contains(i))
                            .findFirst()
                            .getAsInt();
                    int reg = parentFlow.getOrDefault(var, firstAvailableReg);
                    acc.put(var, reg);
                    return acc;
                }, (hm1, hm2) -> {
                    HashMap<Variable, Integer> result = new HashMap<>();
                    result.putAll(hm1);
                    result.putAll(hm2);
                    return result;
                });

        if (!lessOrEq(newFlow, oldFlow)) {
            dataflowOf(state, lub(oldFlow, newFlow));
            return lub(oldFlow, newFlow);
        }

        return null;
    }

    private static Map<Variable, Integer> lub(Map<Variable, Integer> oldFlow, Map<Variable, Integer> newFlow) {
        return newFlow;
    }

    private static boolean lessOrEq(Map<Variable, Integer> s1, Map<Variable, Integer> s2) {
        if (s1 == null) return true;
        if (s2 == null) return false;
        return s1.entrySet().containsAll(s2.entrySet());
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

package analysis;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import petter.cfg.AbstractPropagatingVisitor;
import petter.cfg.CompilationUnit;
import petter.cfg.State;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.edges.Psi;
import petter.cfg.expression.Expression;
import petter.cfg.expression.Variable;
import petter.utils.Tupel;

public class ReachingDefinitionsAnalysis extends
    AbstractPropagatingVisitor<Set<Tupel<Variable, Long>>> {

    // lower upper bound
    static Set<Tupel<Variable, Long>> lub(Set<Tupel<Variable, Long>> s1, Set<Tupel<Variable, Long>> s2) {
        if (s1 == null) return s2;
        if (s2 == null) return s1;
        HashSet<Tupel<Variable, Long>> res = new HashSet<>();
        // Union
        res.addAll(s1);
        res.addAll(s2);
        return res;
    }

    static boolean lessoreq(Set<Tupel<Variable, Long>> s1, Set<Tupel<Variable, Long>> s2) {
        if (s1 == null) return true;
        if (s2 == null) return false;
        return s2.containsAll(s1);
    }

    CompilationUnit cu;

    public ReachingDefinitionsAnalysis(CompilationUnit cu) {
        super(true);
        this.cu = cu;
    }

    public Set<Tupel<Variable, Long>> visit(Assignment assignment,
        Set<Tupel<Variable, Long>> reachingDefinitions) {
        Set<Tupel<Variable, Long>> newDefinitions = new HashSet<>(reachingDefinitions);
        //System.out.println(newDefinitions.size());
        if (assignment.getLhs() instanceof Variable) {
            Variable variable = (Variable) assignment.getLhs();
            newDefinitions.removeIf((rd) -> {
                    boolean res = rd.a.toString().equals(variable.toString());
                    if (res) {
                       // System.out.println("removed " + rd.a.toString() + ", " + rd.b);
                    }
                    return res;
                }
            );
            //System.out.println("remo " + variable.toString() + ", " + newDefinitions.size());
            newDefinitions
                .add(new Tupel<Variable, Long>(variable, assignment.getDest().getId()));
        }
        //System.out.println(newDefinitions.size());
        return newDefinitions;
    }

    public Set<Tupel<Variable, Long>> visit(Psi psi,
                                            Set<Tupel<Variable, Long>> reachingDefinitions) {
        Set<Tupel<Variable, Long>> newDefinitions = new HashSet<>(reachingDefinitions);
        System.out.println(newDefinitions.size());
        for (Expression expression: psi.getLhs()) {
            if (expression instanceof Variable) {
                Variable variable = (Variable) expression;
                newDefinitions.removeIf((rd) -> {
                            boolean res = rd.a.toString().equals(variable.toString());
                            if (res) {
                                 System.out.println("removed " + rd.a.toString() + ", " + rd.b);
                            }
                            return res;
                        }
                );
                //System.out.println("remo " + variable.toString() + ", " + newDefinitions.size());
                newDefinitions
                        .add(new Tupel<Variable, Long>(variable, psi.getDest().getId()));
            }
        }
        System.out.println(newDefinitions.size());
        return newDefinitions;
    }

    public Set<Tupel<Variable, Long>> visit(State state,
        Set<Tupel<Variable, Long>> reachingDefinitions) {
        Set<Tupel<Variable, Long>> oldRD = dataflowOf(state);
        if (oldRD == null) {
            oldRD = new HashSet<>();
            dataflowOf(state, reachingDefinitions);
            if (reachingDefinitions.equals(oldRD)) {
                return reachingDefinitions;
            }
        }

//        System.out.println(state);
//        System.out.println(oldRD);
//        System.out.println(reachingDefinitions);
//        System.out.println(lessoreq(oldRD, reachingDefinitions));
//        System.out.println(lub(reachingDefinitions, oldRD));
        if (!lessoreq(reachingDefinitions, oldRD)) {
            dataflowOf(state, lub(oldRD, reachingDefinitions));
            //System.out.println(state);
            //System.out.println(lub(oldRD, reachingDefinitions));
            return lub(oldRD, reachingDefinitions);
        }
        return null;
    }

    public Set<Tupel<Variable, Long>> visit(ProcedureCall procedureCall,
        Set<Tupel<Variable, Long>> reachingDefinitions) {
//        enter(cu.getProcedure(procedureCall.getCallExpression().getName()), reachingDefinitions);
        return reachingDefinitions;
    }


    String annotateRepresentationOfState(State s) {
//        System.out.println(s);
        String output = dataflowOf(s)
            .stream()
            .map(reachingDefinition -> {
                String str = "[" + reachingDefinition.a.toString() + ", " + reachingDefinition.b
                    + "]";
                return str;
            })
            .collect(Collectors.joining(", "));
//        System.out.println(s);
//        System.out.println(output);
        return output;
    }

    Set<Tupel<Variable, Long>> getStateRepresentation(State s) {
        return dataflowOf(s);
    }
}


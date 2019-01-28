package analysis;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import petter.cfg.AbstractPropagatingVisitor;
import petter.cfg.CompilationUnit;
import petter.cfg.State;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.expression.Variable;
import petter.utils.Tupel;

public class ReachingDefinitionsAnalysis extends
    AbstractPropagatingVisitor<Set<Tupel<Integer, Long>>> {

    CompilationUnit cu;

    public ReachingDefinitionsAnalysis(CompilationUnit cu) {
        super(true);
        this.cu = cu;
    }

    public Set<Tupel<Integer, Long>> visit(Assignment assignment,
        Set<Tupel<Integer, Long>> reachingDefinitions) {
        System.out.println(assignment);
        if (assignment.getLhs() instanceof Variable) {
            int variableId = ((Variable) assignment.getLhs()).getId();
            reachingDefinitions.removeIf((rd) ->
                rd.a == variableId
            );
            reachingDefinitions
                .add(new Tupel<Integer, Long>(variableId, assignment.getDest().getId()));
        }
        return reachingDefinitions;
    }

    public Set<Tupel<Integer, Long>> visit(State state,
        Set<Tupel<Integer, Long>> reachingDefinitions) {
        System.out.println(state);
        System.out.println(state.getOut());
        Set<Tupel<Integer, Long>> oldRD = dataflowOf(state);
        if (oldRD == null) {
            oldRD = new HashSet<>();
            System.out.println(oldRD);
            dataflowOf(state, oldRD);
        }
        if (oldRD.equals(reachingDefinitions)) {
            return null;
        } else {
            oldRD.addAll(reachingDefinitions);
            System.out.println("oldrd was something");
            System.out.println(oldRD);
            dataflowOf(state, oldRD);
            return oldRD;
        }
    }

    public Set<Tupel<Integer, Long>> visit(ProcedureCall procedureCall,
        Set<Tupel<Integer, Long>> reachingDefinitions) {
        //enter(cu.getProcedure(procedureCall.getCallExpression().getName()), reachingDefinitions);
        return null;
    }


    String annotateRepresentationOfState(State s) {
        return "hello";
//        return dataflowOf(s)
//            .stream()
//            .map(reachingDefinition -> "<" + reachingDefinition.a + ", " + reachingDefinition.b
//                + ">")
//            .collect(Collectors.joining(", "));
    }
}


package analysis;

import petter.cfg.*;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.edges.Transition;
import petter.cfg.expression.Expression;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class RegisterAllocationAnalysis extends AbstractPropagatingVisitor<Map<Object, Object>> {
    // Works on SSA form only. Don't compute live ranges of the var
    // Assumes there is only one live range (as if in SSA form)

    CompilationUnit cu;

    public RegisterAllocationAnalysis(CompilationUnit cu) {
        super(true);
        this.cu = cu;
    }

    public Map<Object, Object> visit(ProcedureCall procedureCall, Map<Object, Object> objectObjectMap) {
        return null;
    }

    public Map<Object, Object> visit(State s, Map<Object, Object> newFlow) {
        // let the new-flow have the vals of the previous state
        dataflowOf(s).putAll(newFlow);

        Iterator<Transition> iterator = s.getOutIterator();
        Stream.generate(() -> null)
                .takeWhile(x -> iterator.hasNext())
                .map(n -> iterator.next())
                .map(t -> (Assignment) t)
                .map(Assignment::getLhs)
                .forEach(a -> System.out.println(a.toString()));

//        Assignment a = (Assignment) s.getOutIterator().next();
//        System.out.println(a.getLhs().toString());


//        Boolean oldflow = dataflowOf(s);
//
//        if (!lessoreq(newflow,oldflow)){
//            Boolean newval = lub(oldflow,newflow);
//            dataflowOf(s,newval);
//            return newval;
//        }

        System.out.println("---");
        return newFlow;
    }

    String annotationRepresentationOfState(State s) {
//        ra.dataflowOf(s)
        return "";
    }
}
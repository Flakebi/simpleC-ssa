package analysis.redundancyelimination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import petter.cfg.AbstractVisitor;
import petter.cfg.Analyzable;
import petter.cfg.Procedure;
import petter.cfg.State;
import petter.cfg.TransitionFactory;
import petter.cfg.edges.Transition;
import petter.cfg.expression.Expression;
import petter.cfg.expression.Variable;

public class RedundancyEliminationVisitor1 extends AbstractVisitor {
    // Store true for inserted transitions which should not be touched.
    public static final Object ANNOTATION = new Object();
    private final Procedure p;
    private final AvailableExpressionAnalysis availableExprs;
    private final VeryBusyExpressionAnalysis veryBusyExprs;
    
    private final Set<Analyzable> visited = new HashSet<>();
    private final Map<Expression, Variable> mappings = new HashMap<>();
    
    public RedundancyEliminationVisitor1(Procedure p, AvailableExpressionAnalysis a, VeryBusyExpressionAnalysis v) {
	super(true);
        this.p = p;
        availableExprs = a;
        veryBusyExprs = v;
    }
    
    public Map<Expression, Variable> getMappings() { return mappings; }

    @Override
    protected boolean defaultBehaviour(Analyzable a){
        if (visited.contains(a))
            return false;
        visited.add(a);
	return true;
    }
    
    private static <T> Set<T> removeFrom(T elem, Set<T> set) {
        HashSet<T> res = new HashSet<>();
        res.addAll(set);
        res.remove(elem);
        return res;
    }
    
    @Override
    public boolean visit(State v) {
        if (!defaultBehaviour(v))
            return false;
        for (Expression e : veryBusyExprs.dataflowOf(v)) {
            // Ignore simple expressions
            if (!(e instanceof Variable)) {
                // Iterate over the currently existing transitions, we may add
                // more.
                List<Transition> inTransitions = new ArrayList<>();
                for (Transition t : v.getIn()) {
                    inTransitions.add(t);
                }

                if (v.isBegin()) {
                    // Add also here
                    State newState = new State();
                    availableExprs.dataflowOf(newState, availableExprs.dataflowOf(v));
                    // Remove from very busy
                    veryBusyExprs.dataflowOf(newState, removeFrom(e, veryBusyExprs.dataflowOf(v)));
                    
                    Variable var = new Variable(-1, "T_ " + e, e.getType());
                    mappings.put(e, var);
                    Transition newT = TransitionFactory.createAssignment(newState, v, var, e);
                    newT.putAnnotation(ANNOTATION, true);
                    p.setBegin(newState);
                    visit(newState);
                }
                
                while (!inTransitions.isEmpty()) {
                    Transition t = inTransitions.remove(0);
                    State u = t.getSource();
                    
                    // Compute computeAvailable(t, available(u) union veryBusy(u))
                    Set<Expression> union = new HashSet<>();
                    union.addAll(veryBusyExprs.dataflowOf(u));
                    union.addAll(availableExprs.dataflowOf(u));
                    AvailableExpressionAnalysis.applyTransition(t, union);
                    
                    if (!union.contains(e)) {
                        State newState = new State();
                        availableExprs.dataflowOf(newState, availableExprs.dataflowOf(v));
                        // Remove from very busy
                        veryBusyExprs.dataflowOf(newState, removeFrom(e, veryBusyExprs.dataflowOf(v)));

                        v.deleteInEdge(t);
                        t.setDest(newState);
                        Variable var = new Variable(-1, "T_ " + e, e.getType());
                        mappings.put(e, var);
                        Transition newT = TransitionFactory.createAssignment(newState, v, var, e);
                        newT.putAnnotation(ANNOTATION, true);
                        visit(newState);
                    }
                }
            }
        }
	return true;
    }
}

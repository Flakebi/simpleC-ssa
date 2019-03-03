package analysis.redundancyelimination;

import java.util.HashSet;
import java.util.Set;
import petter.cfg.AbstractVisitor;
import petter.cfg.Analyzable;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.GuardedTransition;
import petter.cfg.edges.Psi;
import petter.cfg.expression.Variable;

public class RedundancyEliminationVisitor2 extends AbstractVisitor {
    private final Set<Analyzable> visited = new HashSet<>();
    private final RedundancyEliminationVisitor1 vis1;

    private RedundancyEliminationVisitor2() { super(true); vis1 = null; }

    public RedundancyEliminationVisitor2(RedundancyEliminationVisitor1 vis1) {
        super(true);
        this.vis1 = vis1;
    }

    @Override
    protected boolean defaultBehaviour(Analyzable a){
        if (visited.contains(a))
            return false;
        visited.add(a);
	return true;
    }

    @Override
    public boolean visit(Assignment a) {
        if (!defaultBehaviour(a))
            return false;
        Boolean te = (Boolean)a.getAnnotation(RedundancyEliminationVisitor1.ANNOTATION);
        if (te == null) {
            // Search variable
            Variable v = vis1.getMappings().get(a.getRhs());
            // We get null for simple expressions
            if (v != null) {
                a.setRhs(v);
            }
        }
        return true;
    }

    @Override
    public boolean visit(Psi a) {
        if (!defaultBehaviour(a))
            return false;
        Boolean te = (Boolean)a.getAnnotation(RedundancyEliminationVisitor1.ANNOTATION);
        if (te == null) {
            for (int i = 0; i < a.getRhs().size(); i++) {
                // Search variable
                Variable v = vis1.getMappings().get(a.getRhs().get(i));
                // We get null for simple expressions
                if (v != null) {
                    a.getRhs().set(i, v);
                }
            }
        }
        return true;
    }

    @Override
    public boolean visit(GuardedTransition a) {
        if (!defaultBehaviour(a))
            return false;
        Boolean te = (Boolean)a.getAnnotation(RedundancyEliminationVisitor1.ANNOTATION);
        if (te == null) {
            // Search variable
            Variable v = vis1.getMappings().get(a.getAssertion());
            // We get null for simple expressions
            if (v != null) {
                a.setAssertion(v);
            }
        }
        return true;
    }
}

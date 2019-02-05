package analysis.constfolding;

import petter.cfg.AbstractPropagatingVisitor;
import petter.cfg.State;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.GuardedTransition;
import petter.cfg.edges.Nop;
import petter.cfg.edges.ProcedureCall;

import java.util.Set;


class ValueAnalysis extends AbstractPropagatingVisitor<Values> {

    private Set<Integer> globalVariables;

    ValueAnalysis(Set<Integer> globalVariables) {
        super(true);
        this.globalVariables = globalVariables;
    }

    @Override
    public Values visit(ProcedureCall ae, Values d) {
        return EdgeEffects.evalProcedureCall(globalVariables, ae, d);
    }

    @Override
    public Values visit(Assignment assignment, Values d) {
        return EdgeEffects.evalAssignment(globalVariables, assignment, d);
    }

    @Override
    public Values visit(Nop nop, Values d) {
        return EdgeEffects.evalNop(globalVariables, nop, d);
    }

    @Override
    public Values visit(GuardedTransition guarded, Values d) {
        return EdgeEffects.evalGuarded(globalVariables, guarded, d);
    }

    @Override
    public Values visit(State s, Values d) {
        final Values dold = dataflowOf(s);
        final Values dnew = Values.join(d, dold);
        if (!Values.isLessEqual(dnew, dold)) {
            dataflowOf(s, dnew);
            return dnew;
        } else {
            return null;
        }
    }
}

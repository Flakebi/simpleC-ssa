package analysis.constfolding;

import petter.cfg.AbstractPropagatingVisitor;
import petter.cfg.State;
import petter.cfg.edges.*;

import java.util.Set;


class ValueAnalysis extends AbstractPropagatingVisitor<Values> {

    ValueAnalysis() {
        super(true);
    }

    @Override
    public Values visit(ProcedureCall ae, Values d) {
        return EdgeEffects.evalProcedureCall(ae, d);
    }

    @Override
    public Values visit(Assignment assignment, Values d) {
        return EdgeEffects.evalAssignment(assignment, d);
    }

    @Override
    public Values visit(Nop nop, Values d) {
        return EdgeEffects.evalNop(nop, d);
    }

    @Override
    public Values visit(GuardedTransition guarded, Values d) {
        return EdgeEffects.evalGuarded(guarded, d);
    }

    @Override
    public Values visit(Psi psi, Values d) {
        return EdgeEffects.evalPsi(psi, d);
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

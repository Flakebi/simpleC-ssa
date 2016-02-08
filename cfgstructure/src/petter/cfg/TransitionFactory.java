package petter.cfg;

import petter.cfg.edges.Assignment;
import petter.cfg.edges.GuardedTransition;
import petter.cfg.edges.Nop;
import petter.cfg.edges.Transition;
import petter.cfg.expression.Expression;
import petter.cfg.expression.FunctionCall;
import petter.cfg.expression.Operator;
import petter.cfg.expression.Variable;

/**
 * Factory for producing CFG edges
 *
 * @author Michael Petter
 */
public class TransitionFactory {

    /**
     * oridinary Assignment Edge
     *
     * @return the edge
     * @see de.tum.in.wwwseidl.programanalysis.cfg.edges.CFGAssignmentEdge
     */
    public static Transition createAssignment(State source, State dest, Expression lhs, Expression rhs) {
        return new Assignment(source, dest, lhs, rhs);
    }

    /**
     * oridinary Null Edge
     *
     * @return the edge
     * @see de.tum.in.wwwseidl.programanalysis.cfg.edges.CFGNullEdge
     */
    public static Transition createNop(State source, State dest) {
        return new Nop(source, dest);
    }

    /**
     * oridinary Assertion Edge
     *
     * @return the edge
     * @see de.tum.in.wwwseidl.programanalysis.cfg.edges.CFGAssertionEdge
     */
    public static Transition createGuard(State source, State dest, Expression assertion, Operator op) {
        return new GuardedTransition(source, dest, assertion, op);
    }

    public static Transition createProcedureCall(State source, State dest, FunctionCall m) {
        return new petter.cfg.edges.ProcedureCall(source, dest, m);
    }
}

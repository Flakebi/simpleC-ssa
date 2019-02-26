package analysis.constfolding;

import petter.cfg.Procedure;
import petter.cfg.State;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.GuardedTransition;
import petter.cfg.edges.Nop;
import petter.cfg.edges.Transition;
import petter.cfg.expression.Expression;
import petter.cfg.expression.Operator;
import petter.cfg.expression.Variable;
import petter.utils.Tupel;

import java.util.LinkedList;
import java.util.Set;

public class ConstFolding {

    private ValueAnalysis analysis;

    public ConstFolding() {
        this.analysis = new ValueAnalysis();
    }

    private static <T> Iterable<T> copyIterable(Iterable<T> xs) {
        final LinkedList<T> ys = new LinkedList<>();
        xs.forEach(ys::add);
        return ys;
    }

    public void fold(Procedure p) {
        for (State state : p.getStates()) {
            analysis.dataflowOf(state, Values.bottom);
        }
        analysis.enter(p, Values.top);
        analysis.fullAnalysis();

        // TODO: Think about implications of SSA-form

        for (State state : copyIterable(p.getStates())) {
            final Values d = analysis.dataflowOf(state);

            if (d.isBottom()) {
                copyIterable(state.getIn()).forEach(Transition::removeEdge);
                copyIterable(state.getOut()).forEach(Transition::removeEdge);
                // The state will be removed by `p.refreshStates` (via reachability analysis).
                continue;
            }

            for (Transition transition : copyIterable(state.getOut())) {
                if (transition instanceof Assignment) {
                    final Variable variable = (Variable) ((Assignment) transition).getLhs();
                    final Expression expression = ((Assignment) transition).getRhs();
                    final Tupel<Expression, Value> exprAndValue = expression.accept(PartialExpressionEvaluator.visitor, d).get();

                    transition.removeEdge();
                    new Assignment(transition.getSource(), transition.getDest(), variable, exprAndValue.a);
                    break;
                }

                if (transition instanceof GuardedTransition) {
                    final Expression expression = ((GuardedTransition) transition).getAssertion();
                    final Operator op = ((GuardedTransition) transition).getOperator();

                    if (ConditionEvaluator.isConditionDefinitellyTrue(expression, op, d)) {
                        transition.removeEdge();
                        new Nop(state, transition.getDest());
                    }
                    break;
                    // The other guarded transition will never be taken. It is removed by the last case.
                }

                final Values dd = EdgeEffects.evalTransition(transition, d);
                if (dd.isBottom()) {
                    transition.removeEdge();
                }
            }
        }

        p.refreshStates();
    }

    public Values getValues(State state) {
        return analysis.dataflowOf(state);
    }
}

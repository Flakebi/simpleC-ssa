package petter.cfg.edges;

import petter.cfg.PropagatingVisitor;
import petter.cfg.State;
import petter.cfg.Visitor;

/**
 *
 * @author stefan
 */
public class ProcedureCall extends Transition {

    private final petter.cfg.expression.FunctionCall m;
        
    /**
     * obtain the call expression
     * @return the call expression
     */
    public petter.cfg.expression.FunctionCall getCallExpression() { 
        return m; 
    }

    
    public ProcedureCall(State source, State dest, petter.cfg.expression.FunctionCall m) {
        super(source, dest);
        this.m = m;
    }

    @Override
    public String toString() {
        return m.toString();
    }

    // interface Analyzable:
    @Override
    public void forwardAccept(Visitor v) {
        if (v.visit(this)) {
            v.enter(dest);
        }
    }

    @Override
    public void backwardAccept(Visitor v) {
        if (v.visit(this)) {
            v.enter(source);
        }
    }

    @Override
    public <T> void forwardAccept(PropagatingVisitor<T> v, T d) {
        if ((d = v.visit(this, d)) != null) {
            v.enter(dest, d);
        }
    }

    @Override
    public <T> void backwardAccept(PropagatingVisitor<T> v, T d) {
        if ((d = v.visit(this, d)) != null) {
            v.enter(source, d);
        }
    }
    // interface Analyzable end
}

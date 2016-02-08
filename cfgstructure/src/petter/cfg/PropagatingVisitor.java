package petter.cfg;
import petter.cfg.edges.Nop;
import petter.cfg.edges.GuardedTransition;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.ProcedureCall;
/**
 * the basic interface for all advanced visitors.
 * You have to implement this interface to visit each node in a CFG. If You want to implement fixpoint algorithms, You could inherit from the abstract class {@link AbstractCFGPropagatingVisitor}. This specific interface here provides the possibility to pass on an additional data object with each step in the CFG. The only restriction on this data object is to implement the 
 * @author Michael Petter
 * @see AbstractCFGPropagatingVisitor
 * @see Analyzable
 */
public interface PropagatingVisitor<T>{
    /**
     * schedules a new CFG-element for visiting.
     * This method enables the concrete visitor to control it's way through the CFG, and do it for example stepwise. In this visitor, You also have to specify a custom data object d.
     * @param a the CFG-element to be scheduled
     * @param d the data to be passed on
     */
    public void enter(Analyzable a,T d);
    /**
     * specific visit method.
     * Override this method to provide custom actions when traversing a {@link CFGState}. In this visitor, You also have to specify a custom data object d.
     * @param s the CFGState which is visited
     * @param d the data to be passed on
     * @return the new data to be propagated in the CFG when the iteration has to continue, <code>null</code> when the iteration stabilizes
     */
    public T visit(State s,T d);
    /**
     * specific visit method.
     * Override this method to provide custom actions when traversing a {@link CFGNullEdge}. In this visitor, You also have to specify a custom data object d.
     * @param ne the CFGNullEdge which is visited
     * @param d the data to be passed on
     * @return the new data to be propagated in the CFG when the iteration has to continue, <code>null</code> when the iteration stabilizes
     */
    public T visit(Nop ne, T d);
    /**
     * specific visit method.
     * Override this method to provide custom actions when traversing a {@link CFGAssignmentEdge}. In this visitor, You also have to specify a custom data object d.
     * @param ae the CFGAssignmentEdge which is visited
     * @param d the data to be passed on
     * @return the new data to be propagated in the CFG when the iteration has to continue, <code>null</code> when the iteration stabilizes
     */
    public T visit(Assignment ae, T d);
    /**
     * specific visit method.
     * Override this method to provide custom actions when traversing a {@link CFGAssertionEdge}. In this visitor, You also have to specify a custom data object d.
     * @param ae the CFGAssertionEdge which is visited
     * @param d the data to be passed on
     * @return the new data to be propagated in the CFG when the iteration has to continue, <code>null</code> when the iteration stabilizes
     */
    public T visit(GuardedTransition ae, T d);
    /**
     * specific visit method.
     * Override this method to provide custom actions when traversing a {@link CFGMethode}. In this visitor, You also have to specify a custom data object d.
     * @param ae the CFGMethode which is visited
     * @param d the data to be passed on
     * @return the new data to be propagated in the CFG when the iteration has to continue, <code>null</code> when the iteration stabilizes
     */
    public T visit(Procedure ae, T d);
    
    public T visit(ProcedureCall ae, T d);
}

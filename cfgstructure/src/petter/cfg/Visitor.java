package petter.cfg;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.GuardedTransition;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.edges.Nop;
/**
 * the basic interface for all simple visitors.
 * You have to implement this interface to visit each node in a CFG. If You want to implement fixpoint algorithms, You could inherit from the abstract class {@link AbstractCFGVisitor}.
 * @author Michael Petter
 * @see AbstractCFGVisitor
 * @see Analyzable
 */
public interface Visitor{
    /**
     * schedules a new CFG-element for visiting.
     * This method enables the concrete visitor to control it's way through the CFG, and do it for example stepwise
     * @param a the CFG-element to be scheduled
     */
    public void enter(Analyzable a);
    /**
     * specific visit method.
     * Override this method to provide custom actions when traversing a {@link CFGState}
     * @param s the CFGState which is visited
     * @return <code>true</true> when the iteration has to continue, <code>false</code> when the iteration stabilizes
     */
    public boolean visit(State s);
    /**
     * specific visit method.
     * Override this method to provide custom actions when traversing a {@link CFGNullEdge}
     * @param ne the CFGNullEdge which is visited
     * @return <code>true</true> when the iteration has to continue, <code>false</code> when the iteration stabilizes
     */
    public boolean visit(Nop ne);
    /**
     * specific visit method.
     * Override this method to provide custom actions when traversing a {@link CFGAssignmentEdge}
     * @param ae the CFGAssignmentEdge which is visited
     * @return <code>true</true> when the iteration has to continue, <code>false</code> when the iteration stabilizes
     */
    public boolean visit(Assignment ae);
    /**
     * specific visit method.
     * Override this method to provide custom actions when traversing a {@link CFGAssertionEdge}
     * @param ae the CFGAssertionEdge which is visited
     * @return <code>true</true> when the iteration has to continue, <code>false</code> when the iteration stabilizes
     */
    public boolean visit(GuardedTransition ae);
    /**
     * specific visit method.
     * Override this method to provide custom actions when traversing a {@link CFGMethode}
     * @param ae the CFGMethode which is visited
     * @return <code>true</true> when the iteration has to continue, <code>false</code> when the iteration stabilizes
     */
    public boolean visit(Procedure ae);
    //public boolean visit(CFGLoopSeparator s);
    public boolean visit(ProcedureCall ae);
}

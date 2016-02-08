package petter.cfg;
import petter.cfg.edges.Nop;
import petter.cfg.edges.GuardedTransition;
import petter.cfg.edges.Assignment;

import java.util.Queue;
import java.util.Iterator;
import java.util.LinkedList;
import petter.cfg.edges.ProcedureCall;
/**
 * provides a generic interface to constructing a fixpoint iteration algorithm.
 * You would have to extend an own class from this one to get an own fixpoint algorithm. The Visitor performs a run through the whole CFG, as long as it's visit methods return true; to terminate a run, You have to ensure, that the return value of a visit method becomes false at some point.
 * @ru.k2s.example Counting all states of a CFG.
 * import java.util.*;
 * import de.tum.in.wwwseidl.programanalysis.cfg.*;
 * class SimpleFixpointAlgorithm extends AbstractCFGVisitor{
 *   private Set memory = new HashSet();
 *   private int counter=0;
 *   public static int countStates(CFGState begin){
 *     SimpleFixpointAlgorithm fa = new SimpleFixpointAlgorithm();
 *     fa.enter(begin);
 *     fa.fullAnalysis();
 *     return fa.counter;
 *   }
 *   private SimpleFixpointAlgorithm() {
 *     super(true); // to get a forward analysis
 *   }
 *   public boolean visit(CFGState s){
 *     if (memory.contains(s)) return false;
 *     counter++;
 *     memory.addElement(s);
 *     return true;
 *   }
 * }
 * @see CFGVisitor
 * @see Analyzable
 * @author Michael Petter
 */
public abstract class AbstractVisitor implements Visitor{
    private Queue<Analyzable> q = new LinkedList<Analyzable>();
    private boolean direction = true;
    /**
     * construct an new fixpoint iteration facility.
     * You can determine the analysis direction here in the constructor. Be careful: <code>protected</code> constructors can't be called from everywhere!
     * @param directionForward <code>true</code> when forward analysis, <code>false</code> when backward analysis
     */
    protected AbstractVisitor(boolean directionForward){
	direction=directionForward;
    }

    /**
     * a means to test for termination / stabilization.
     * Is there still a step in the internal queue left?
     * @return <code>true</code>, when there is still an item in the queue
     */
    public boolean hasNext(){
	return (q.peek()!=null);
    }
    /**
     * override this method for a hook in the polling process
     */
    protected Analyzable poll(){
        return q.poll();
    }
    /**
     * override this method for a hook in the polling process
     */
    protected Analyzable peek(){
        return q.peek();
    }
    /**
     * one step of the analysis is processed here.
     * You can trigger the execution of one single step of the analysis here, resulting in a call to the corresponding visit method
     * @return <code>true</code>, when there is still some item left in the queue
     */
    protected boolean processNext(){
	if (!hasNext()) return false;
	// implicitely q.peek() returned a valid object
	Analyzable a = poll();
	if (direction) a.forwardAccept(this);
	else a.backwardAccept(this);
	return true;
    }

    /**
     * delivers a textual representation of the queue.
     * Mainly used for debugging and animation purposes
     * @return a String array of all remaining items in the queue
     */
    public String[] getQueue(){
	String [] ret = new String[q.size()];
	int i =0;
	Iterator it = q.iterator();
	while (it.hasNext()){
	    ret[i++]=it.next().toString();
	}
	return ret;
    }
    
    /**
     * performs a full fixpoint iteration.
     * And stops if there is no item in the queue left
     */
    public void fullAnalysis(){
	while (processNext()) ;
    }
    
    /**
     * provides a standardized return value for all not overwritten visit methods.
     * the default value here is true, meaning, that a not overwritten visit method just passes on to the next item in the queue
     * @param a the item, which is visited; can be ignored
     * @return a default value for visit methods
     */
    protected boolean defaultBehaviour(Analyzable a){
	return true;
    }

    /**
     * Enters the provided item in the queue of the fixpoint iteration
     * @param a the item to add
     */
    public void enter(Analyzable a) {
	q.offer(a);
    }
    public boolean visit(State s)                 { return defaultBehaviour(s); }
    public boolean visit(Nop s)              { return defaultBehaviour(s); }
    public boolean visit(Assignment s) 	     { return defaultBehaviour(s); }
    public boolean visit(GuardedTransition s)	     { return defaultBehaviour(s); }
    public boolean visit(Procedure s)	     { return defaultBehaviour(s); }
    public boolean visit(ProcedureCall s)	     { return defaultBehaviour(s); }
}

package petter.cfg.edges;
import petter.cfg.PropagatingVisitor;
import petter.cfg.State;
import petter.cfg.Visitor;
import petter.cfg.expression.Expression;
import petter.cfg.expression.Variable;
import java.util.List;
/**
 * represents an AssignmentEdge in the CFG
 */
public class Psi extends Transition {
    private List<Expression> lhs;
    private List<Expression> rhs;
    /**
     * construct an new CFGAssignmentEdge
     * @param source  start of edge
     * @param dest    end of edge
     * @param lhs    lefthandside of an AssignmentEdge is a variable
     * @param rhs   righthandside of an AssignmentEdge is an expression
     */
    public Psi(State source, State dest, List<Expression> lhs, List<Expression> rhs){
        super(source,dest);
        this.lhs=lhs;
        this.rhs=rhs;
    }
    /**
     * obtain lefthandside of Assignment
     * @return a Variable
     */
    public List<Expression> getLhs(){
        return lhs;
    }
    /**
     * obtain righthandside of Assignment
     * @return an Expression
     */
    public List<Expression> getRhs(){
        return rhs;
    }
    /**
     * set lefthandside of Assignment.
     */
    public void setLhs(List<Expression> lhs){
        this.lhs = lhs;
    }
    /**
     * set righthandside of Assignment.
     */
    public void setRhs(List<Expression> rhs){
        this.rhs = rhs;
    }
    /**
     * string representation of the Assignment
     * @return guess what?
     */
    public String toString(){
        return "";
    }
    // interface Analyzable:
    public void forwardAccept(Visitor v){
        if (v.visit(this)) v.enter(dest);
    }
    public void backwardAccept(Visitor v){
        if (v.visit(this)) v.enter(source);
    }
    public <T>void forwardAccept(PropagatingVisitor<T> v,T d){
        if ((d=v.visit(this,d))!=null) v.enter(dest,d);
    }
    public <T>void backwardAccept(PropagatingVisitor<T> v, T d){
        if ((d=v.visit(this,d))!=null) v.enter(source,d);
    }
    // interface Analyzable end
}

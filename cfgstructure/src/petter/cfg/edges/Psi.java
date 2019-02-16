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

    public void addExpr(Expression lhs, Expression rhs) {
        this.lhs.add(lhs);
        this.rhs.add(rhs);
    }

    public void setOneLhs(Expression lhs, int ind) {
        this.lhs.set(ind, lhs);
    }

    public void setOneRhs(Expression rhs, int ind) {
        this.rhs.set(ind, rhs);
    }

    public String toString(){
        String s = "";
        for (int i = 0; i < lhs.size(); i++)
            s += lhs.get(i).toString() + " = " + rhs.get(i).toString() + ";\r\n";
        return s;
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

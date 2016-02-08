package petter.cfg.expression.visitors;

import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;
import petter.cfg.expression.BinaryExpression;
import petter.cfg.expression.Expression;
import petter.cfg.expression.FunctionCall;
import petter.cfg.expression.IntegerConstant;
import petter.cfg.expression.StringLiteral;
import petter.cfg.expression.UnaryExpression;
import petter.cfg.expression.UnknownExpression;
import petter.cfg.expression.Variable;

/**
 * provides an abstract class to visit an expression in DFS, while passing a propagated value around;
 * @see ExpressionVisitor
 * @author Michael Petter
 * @author Daniel Stuewe
 */
public abstract class DefaultUpDownDFS<T> implements PropagatingDFS<T, T> {

    protected T defaultBehaviour(Expression e,T fromParent){
	return fromParent;
    }
    protected Optional<T> defaultBehaviourPre(Expression e,T fromParent){
        return Optional.of(fromParent);
    }
    
    @Override public Optional<T> preVisit(StringLiteral s, T fromParent){return defaultBehaviourPre(s,fromParent);}
    @Override public Optional<T> preVisit(IntegerConstant s, T fromParent){return defaultBehaviourPre(s,fromParent);}
    @Override public Optional<T> preVisit(Variable s, T fromParent){return defaultBehaviourPre(s,fromParent);}
    @Override public Optional<T> preVisit(FunctionCall s, T fromParent){return defaultBehaviourPre(s,fromParent);}
    @Override public Optional<T> preVisit(UnknownExpression s, T fromParent){return defaultBehaviourPre(s,fromParent);}
    @Override public Optional<T> preVisit(UnaryExpression s, T fromParent){return defaultBehaviourPre(s,fromParent);}
    @Override public Optional<T> preVisit(BinaryExpression s, T fromParent){return defaultBehaviourPre(s,fromParent);}
    @Override public T postVisit(IntegerConstant s, T fromParent){ return defaultBehaviour(s,fromParent); }
    @Override public T postVisit(StringLiteral s, T fromParent){return defaultBehaviour(s,fromParent);}
    @Override public T postVisit(Variable s, T fromParent){return defaultBehaviour(s,fromParent);}
    @Override public T postVisit(UnknownExpression s, T fromParent){return defaultBehaviour(s,fromParent);}
    @Override public T postVisit(UnaryExpression s, T fromChild){return defaultBehaviour(s,fromChild);}
    public static abstract class Joining<T> extends DefaultUpDownDFS<T>{
        BinaryOperator<T> join;
        public Joining(BinaryOperator<T> join) {
            this.join=join;
        }
        @Override
        public T postVisit(BinaryExpression s, T lhs, T rhs) {
            return defaultBehaviour(s, join.apply(lhs,rhs));
        }

        @Override
        public T postVisit(FunctionCall m, T fromTop, Stream<T> it) {
            return it.reduce(join).orElse(fromTop);
        }
        
    }
}

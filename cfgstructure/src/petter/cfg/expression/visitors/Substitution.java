/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package petter.cfg.expression.visitors;

import java.util.function.BinaryOperator;
import java.util.stream.Stream;
import java.util.Map;
import java.util.stream.Collectors;
import petter.cfg.expression.BinaryExpression;
import petter.cfg.expression.Expression;
import petter.cfg.expression.FunctionCall;
import petter.cfg.expression.UnaryExpression;
import petter.cfg.expression.Variable;

/**
 *
 * @author petter
 */
public class Substitution extends DefaultUpDownDFS<Expression>{

    public static Expression subst(Expression ex,Map<Variable, Expression> map){
        Expression result;
        Substitution dfs = new Substitution(map);
        result = ex.accept(dfs,null).get();
        return result;
    }
    
    private final Map<Variable, Expression> sigma;
    private Substitution(Map<Variable,Expression> map) {
        this.sigma=map;
    }
    @Override
    public Expression postVisit(FunctionCall m, Expression s, Stream<Expression> it) {
        return new FunctionCall(m.getName(),m.getType(),it.collect(Collectors.toList()));
    }
    @Override
    public Expression postVisit(BinaryExpression s, Expression lhs, Expression rhs) {
        return new BinaryExpression(lhs, s.getOperator(), rhs);
    }
    @Override
    public Expression postVisit(UnaryExpression s, Expression fromChild) {
        return new UnaryExpression(fromChild, s.getOperator());
    }
    @Override
    public Expression postVisit(Variable s, Expression fromParent) {
        return sigma.containsKey(s) ? sigma.get(s):s;
    }
}

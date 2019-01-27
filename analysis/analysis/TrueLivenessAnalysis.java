package analysis;

import petter.cfg.*;
import petter.cfg.edges.*;
import petter.cfg.expression.BinaryExpression;
import petter.cfg.expression.Expression;
import petter.cfg.expression.Variable;
import petter.cfg.expression.visitors.AbstractExpressionVisitor;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

public class TrueLivenessAnalysis extends AbstractPropagatingVisitor<Set<Expression>> {
    private CompilationUnit cu;

    public TrueLivenessAnalysis(CompilationUnit cu) {
        super(false);
        this.cu = cu;
    }

    public Set<Expression> visit(ProcedureCall procedureCall, Set<Expression> expressionSet) {
        return null;
    }

    public Set<Expression> visit(State s, Set<Expression> newFlow) {
        // let the new-flow have the vals of the previous state
        Set<Expression> result = new HashSet<>(newFlow);

        s.getIn().forEach(t -> t.backwardAccept(new AbstractVisitor(false) {
            @Override
            public boolean visit(GuardedTransition s) {
                System.out.println(s.toString());
                return super.visit(s);
            }

            @Override
            public boolean visit(Assignment s) {
                if (result.contains(s.getLhs())) {
                    s.getRhs().accept(new AbstractExpressionVisitor() {
                        @Override
                        public void postVisit(Variable s) {
                            result.add(s);
                            super.postVisit(s);
                        }

                        @Override
                        public void postVisit(BinaryExpression s) {
                            result.add(s);
                            super.postVisit(s);
                        }
                    });
                }

                result.remove(s.getLhs());
                return super.visit(s);
            }
        }));


        System.out.println(s.toString());
        System.out.println(newFlow.toString());
        System.out.println(result.toString());
        System.out.println("---");

        return result;
    }

    String annotationRepresentationOfState(State s) {
        System.out.println(dataflowOf(s));
        return "";
    }

    public static void main(String[] args) throws Exception {
        String filePath = "/Users/denis.g/Developer/simpleC-ssa/analysis/TrueLivenessFiles/" + "test.c";
        CompilationUnit cu = petter.simplec.Compiler.parse(new File(filePath));

        TrueLivenessAnalysis la = new TrueLivenessAnalysis(cu);
        Procedure main = cu.getProcedure("main");
        DotLayout layout = new DotLayout("jpg","main.jpg");

//        main.get
//
//        cu.getGlobals().forEach(id -> System.out.println(cu.getVariableName(id)));

        Set<Expression> liveExprs = new HashSet<Expression>();
        Variable returnVar = new Variable(0, "return", null);
        liveExprs.add(returnVar);

        la.enter(main, liveExprs);
        la.fullAnalysis();

        main.getStates().forEach(s -> layout.highlight(s, la.annotationRepresentationOfState(s)));

        layout.callDot(main);
    }
}
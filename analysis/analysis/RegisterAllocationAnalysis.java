package analysis;

import petter.cfg.*;
import petter.cfg.edges.ProcedureCall;

import java.io.File;


public class RegisterAllocationAnalysis extends AbstractPropagatingVisitor<Boolean>{

    static Boolean lub(Boolean b1,Boolean b2){
        if (b1==null) return b2;
        if (b2==null) return b1;
        return b1||b2;
    }
    static boolean lessoreq(Boolean b1,Boolean b2){
        if (b1==null) return true;
        if (b2==null) return false;
        return ((!b1) || b2);
    }

    CompilationUnit cu;

    public RegisterAllocationAnalysis(CompilationUnit cu){
        super(true); // forward reachability
        this.cu=cu;
    }

    public Boolean visit(ProcedureCall m, Boolean b){
        // method calls need special attention; in this case, we just 
        // continue with analysing the next state and triggering the analysis
        // of the callee
        enter(cu.getProcedure(m.getCallExpression().getName()),true);
        return b;
    }

    public Boolean visit(State s, Boolean newflow){
        Boolean oldflow = dataflowOf(s);

        if (!lessoreq(newflow,oldflow)){
            Boolean newval = lub(oldflow,newflow);
            dataflowOf(s,newval);
            return newval;
        }

        return null;
    }

    public static void main(String[] args) throws Exception {
        String filePath = "/Users/denis.g/Developer/simpleC-ssa/analysis/RegisterAllocationFiles/" + "test.c";
        CompilationUnit cu = petter.simplec.Compiler.parse(new File(filePath));

        RegisterAllocationAnalysis ra = new RegisterAllocationAnalysis(cu);
        Procedure foo = cu.getProcedure("main");
        DotLayout layout = new DotLayout("jpg","main.jpg");

        ra.enter(foo,true);
        ra.fullAnalysis();

        for (State s: foo.getStates()){
            layout.highlight(s,(ra.dataflowOf(s))+"");
        }

        layout.callDot(foo);
    }
}
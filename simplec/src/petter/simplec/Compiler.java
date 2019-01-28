package petter.simplec;
import petter.constfolding.*;
import petter.cfg.CompilationUnit;
import petter.cfg.Procedure;
import petter.cfg.State;
import petter.cfg.edges.Transition;
import petter.utils.AnnotatingSymbolFactory;
import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import petter.cfg.DotLayout;
public class Compiler{

    public static CompilationUnit parse(File f) throws Exception{
            InputStream is = new FileInputStream(f);
            AnnotatingSymbolFactory sf = new AnnotatingSymbolFactory(f);
            Parser parser = new Parser(new Lexer(is,sf),sf);
            CompilationUnit cu = (CompilationUnit)parser.parse().value;
            for (Procedure p:cu){
            	for (State s:p.getStates())
            		if (s.getInDegree()==0 && !s.isBegin())
            			System.out.println(p.getName()+" found: "+s);
            	for (Transition t: p.getTransitions()){
            		if (t.getSource().getInDegree()==0 && !t.getSource().isBegin()){
            			System.out.println(p.getName()+" found: "+t);
            		}
            	}
            }
            return cu;
        
    }

    public static void main(String[] args){
        if (args.length<1) {
            System.out.println("Usage: java -jar Compiler.jar input.simplec");
            return;
        }
        try{
            CompilationUnit c = parse(new File(args[0]));
            final Map<String, Procedure> procedures = c.getProcedures();

            final Procedure main = procedures.get("main");
            final DotLayout layout = new DotLayout("png",args[0]+"."+main.getName()+".orig.png");
            layout.callDot(main);

            final Set<Integer> globalVariables = new HashSet<>(c.getGlobals());
            ConstFolding constfold = new ConstFolding(globalVariables);
            constfold.fold(main);

            final DotLayout layout2 = new DotLayout("png",args[0]+"."+main.getName()+".const.png");
            for (State state : main.getStates()) {
                layout2.highlight(state, constfold.getValues(state).toString());
            }
            layout2.callDot(main);
        }catch(FileNotFoundException fnfe){
            fnfe.printStackTrace();
            return;
        }
        catch (Exception e){
            e.printStackTrace();
            return;
        }
    }
}

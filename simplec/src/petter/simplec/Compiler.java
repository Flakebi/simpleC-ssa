package petter.simplec;
import petter.cfg.CompilationUnit;
import petter.cfg.Procedure;
import petter.cfg.State;
import petter.cfg.edges.Transition;
import petter.utils.AnnotatingSymbolFactory;
import petter.simplec.Lexer;
import petter.simplec.Parser;
import java_cup.runtime.*;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

import petter.cfg.DotLayout;

public class Compiler {
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
}

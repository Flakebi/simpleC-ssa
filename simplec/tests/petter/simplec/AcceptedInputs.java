package petter.simplec;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import petter.cfg.CompilationUnit;
import petter.utils.AnnotatingSymbolFactory;

public class AcceptedInputs {
	private static CompilationUnit compile(String filename) throws Exception{
		InputStream is = new FileInputStream("testfiles/correct/"+filename);
        AnnotatingSymbolFactory sf = new AnnotatingSymbolFactory();
        Parser parser = new Parser(new Lexer(is,sf),sf);
        return (CompilationUnit)parser.parse().value;
	}
	@Test
	public void animation() {
		try{
			compile("animation.c");
		}catch(Exception e){
			e.printStackTrace();
			fail("unexpected Exception "+e);
		}
	}@Test
	public void classical() {
		try{
			compile("classical.c");
		}catch(Exception e){
			e.printStackTrace();
			fail("unexpected Exception "+e);
		}
	}@Test
	public void oldtest() {
		try{
			compile("oldtest.c");
		}catch(Exception e){
			e.printStackTrace();
			fail("unexpected Exception "+e);
		}
	}@Test
	public void typechaos() {
		try{
			compile("typechaos.c");
		}catch(Exception e){
			e.printStackTrace();
			fail("unexpected Exception "+e);
		}
	}
	@Test
	public void uninitialized() {
		try{
			compile("uninitialized.c");
		}catch(Exception e){
			e.printStackTrace();
			fail("unexpected Exception "+e);
		}
	}

}

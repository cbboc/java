package cbboc.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import cbboc.ProblemInstance;

public class TestProblemInstance {

	@Test
	public void test() throws FileNotFoundException, IOException {
		
		String root = System.getProperty( "user.dir" );
		String path = root + "/resources/test/toy/testing/toy.txt";
		ProblemInstance instance = new ProblemInstance( new FileInputStream( new File( path ) ) );
		assertEquals( 8, instance.getNumGenes() );
		assertEquals( 100000, instance.getMaxEvalsPerInstance() );
	}
}

// End ///////////////////////////////////////////////////////////////

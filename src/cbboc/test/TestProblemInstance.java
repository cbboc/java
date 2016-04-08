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
	public void test1() throws FileNotFoundException, IOException {
		String root = System.getProperty( "user.dir" );
		String path = root + "/resources/toy1.txt";
		ProblemInstance instance = new ProblemInstance( new FileInputStream( new File( path ) ) );
		assertEquals( 8, instance.getNumGenes() );
		// assertEquals( 100000, instance.getMaxEvalsPerInstance() );
	}

	@Test
	public void test2() throws FileNotFoundException, IOException {
		String root = System.getProperty( "user.dir" );
		String path = root + "/resources/toy2.txt";
		ProblemInstance instance = new ProblemInstance( new FileInputStream( new File( path ) ) );
		assertEquals( 13, instance.getNumGenes() );
		// assertEquals( 100000, instance.getMaxEvalsPerInstance() );
	}
}

// End ///////////////////////////////////////////////////////////////

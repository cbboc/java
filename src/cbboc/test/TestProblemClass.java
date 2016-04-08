package cbboc.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import cbboc.ProblemClass;
import cbboc.ProblemInstance;
import cbboc.TrainingCategory;

public class TestProblemClass {

	@Test
	public void test() throws IOException {
		String root = System.getProperty( "user.dir" );
		String path = root + "/resources/sample1";
		
		ProblemClass noTraining = new ProblemClass( Paths.get( path ), TrainingCategory.NONE );
		assertEquals( 0, noTraining.getTrainingInstances().size() );
		assertEquals( 50, noTraining.getTestingInstances().size() );
		
		ProblemClass shortTraining = new ProblemClass( Paths.get( path ), TrainingCategory.SHORT );
		assertEquals( 200, shortTraining.getTrainingInstances().size() );
		assertEquals( 50, shortTraining.getTestingInstances().size() );		

		ProblemClass longTraining = new ProblemClass( Paths.get( path ), TrainingCategory.LONG );
		assertEquals( 200, longTraining.getTrainingInstances().size() );
		assertEquals( 50, longTraining.getTestingInstances().size() );		
	}
}

// End //////////////////////////////////////////////////////////////


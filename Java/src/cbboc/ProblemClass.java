package cbboc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import jeep.lang.Diag;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.Gson;

public final class ProblemClass {

	private TrainingCategory trainingCategory;
	private final List< ObjectiveFn > training = new ArrayList< ObjectiveFn >();	
	private final List< ObjectiveFn > testing  = new ArrayList< ObjectiveFn >();
	
	///////////////////////////////

	private static List< String > 
	readInstances( File testingFilesTxt ) throws IOException {	
		if( !testingFilesTxt.exists() )
			throw new IllegalArgumentException();
		
		InputStream is = new FileInputStream( testingFilesTxt );
		LineNumberReader r = new LineNumberReader( 
			new BufferedReader( new InputStreamReader( is ) ) );

		String line = r.readLine();
		Scanner scanner = new Scanner( line );
		final int numInstances;
		try {
			numInstances = scanner.nextInt();
		}
		finally {
			scanner.close();			
		}
		
		try {
			List< String > result = new ArrayList< String >();
			for( int i=0; i<numInstances; ++i )
				result.add( r.readLine() );				
			
			assert result.size() == numInstances;
			return result;
		}
		finally {
			r.close();
		}
	}
	
	///////////////////////////////	
	
	public ProblemClass( Path root, TrainingCategory trainingCategory ) throws IOException {
		
		this.trainingCategory = trainingCategory;
		File trainingFilesInventory = new File( root + "/trainingFiles.txt" );
		if( !trainingFilesInventory.exists() )
			throw new RuntimeException( "Fatal problem class file error: cannot find " + trainingFilesInventory );
		
		File testingFilesInventory = new File( root + "/testingFiles.txt" );
		if( !testingFilesInventory.exists() )
			throw new RuntimeException( "Fatal problem class file error: cannot find " + testingFilesInventory );
		
		List< String > trainingFiles = readInstances( trainingFilesInventory );
		List< String > testingFiles = readInstances( testingFilesInventory );
		
		///////////////////////////
		
		List< ProblemInstance > trainingInstances = new ArrayList< ProblemInstance >();		
		switch( trainingCategory ) {
			case NONE : // Intentionally Empty
			break;		
			case SHORT :
			case LONG :				
				for( String s : trainingFiles )
				trainingInstances.add( new ProblemInstance( new FileInputStream( root + "/" + s ) ) );
				break;		
		}
		
		int totalTrainingEvaluations = 0;
		for( ProblemInstance p : trainingInstances )
			totalTrainingEvaluations += p.getMaxEvalsPerInstance();
		
		totalTrainingEvaluations *= trainingCategory.getMultiplier();
		
		///////////////////////////
		
		MutableLong sharedTrainingEvaluations = new MutableLong( totalTrainingEvaluations ); 
		for( ProblemInstance p : trainingInstances )
			training.add( new ObjectiveFn( p, ObjectiveFn.TimingMode.TRAINING, sharedTrainingEvaluations ) );

		///////////////////////////

		List< ProblemInstance > testingInstances = new ArrayList< ProblemInstance >();
		for( String f : testingFiles )
			testingInstances.add( new ProblemInstance( new FileInputStream( root + "/" + f ) ) );
		
		for( ProblemInstance p : testingInstances ) {
			MutableLong individualTestingEvaluations = new MutableLong( p.getMaxEvalsPerInstance() );			
			testing.add( new ObjectiveFn( p, ObjectiveFn.TimingMode.TESTING, individualTestingEvaluations ) );
		}
	}
	
	///////////////////////////////
	
	public TrainingCategory getTrainingCategory() { return trainingCategory; }
	public List< ObjectiveFn > getTrainingInstances() { return training; }
	public List< ObjectiveFn > getTestingInstances() { return testing; }
	
	///////////////////////////////	
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString( this, ToStringStyle.MULTI_LINE_STYLE );
	}
}

// End ///////////////////////////////////////////////////////////////


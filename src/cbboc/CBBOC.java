package cbboc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import jeep.lang.Diag;

import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.Gson;

//////////////////////////////////////////////////////////////////////

public final class CBBOC {

	private final static long BASE_TIME_PER_INSTANCE_IN_MILLIS = 250 * 1000L;	
	public static Logger LOGGER = Logger.getLogger( CBBOC.class.getName() );

	public static final boolean LOGGING_ENABLED = false;
	static {
		if( !LOGGING_ENABLED )
			LogManager.getLogManager().reset();
	}
	
	////////////////////////////////
	
	static long trainingEndTime = -1;
	static long testingEndTime = -1;	
	
	////////////////////////////////
	
	static final class EvaluationsExceededException extends RuntimeException {
		private static final long serialVersionUID = 1L;		
	}
	
	static final class TimeExceededException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}	
	
	////////////////////////////////
	
	private static boolean allSameN( List< ObjectiveFn > p ) {
		final int N = p.get(0).getNumGenes();
		for( int i=1; i<p.size(); ++i )
			if( p.get( i ).getNumGenes() != N )
				return false;
		
		return true;
	}

	////////////////////////////////	

	private static long trainClient( Competitor client, List< ObjectiveFn > p ) {
		assert( allSameN( p ) );

		final long startTime = System.currentTimeMillis();	
		final long maxTime = BASE_TIME_PER_INSTANCE_IN_MILLIS * p.size() * client.getTrainingCategory().getMultiplier();
		trainingEndTime = startTime + maxTime;
		
		try {
			client.train( p, maxTime );
		}
		catch( TimeExceededException | EvaluationsExceededException ex ) {
			// Intentionally Empty
		}
		
		final long endTime = System.currentTimeMillis();
		return endTime - startTime;
	}

	////////////////////////////////
	
	private static long testClient( Competitor client, List< ObjectiveFn > fns ) {
		
		final long startTime = System.currentTimeMillis();		

		for( ObjectiveFn fn : fns ) {
			try {
				final long now = System.currentTimeMillis();
				final long maxTime = BASE_TIME_PER_INSTANCE_IN_MILLIS;
				
				testingEndTime = now + maxTime;
				client.test( fn, maxTime );
			}
			catch( TimeExceededException | EvaluationsExceededException ex ) {
				// Intentionally Empty
			}
		}
		
		final long endTime = System.currentTimeMillis();
		return endTime - startTime;
	}
	
	////////////////////////////////
	
	private static final class OutputResults {
		
		final String competitorName;
		final String competitorLanguage = "Java";
		final String problemClassName;		
		// final String trainingCategory;
		final TrainingCategory trainingCategory;		
		final String datetime;
		final List< Result > trainingResults = new ArrayList< Result >();
		final long trainingWallClockUsage;
		final List< Result > testingResults  = new ArrayList< Result >();
		final long testingWallClockUsage;
		
		///////////////////////////

		private static final class Result {
			private final long remainingEvaluations;			
			private final long remainingEvaluationsWhenBestReached;
			private final double bestValue;
			
			///////////////////////////
			
			public Result( long remainingEvaluations, long remainingEvaluationsWhenBestReached, double bestValue ) {
				this.remainingEvaluations = remainingEvaluations; 
				this.remainingEvaluationsWhenBestReached = remainingEvaluationsWhenBestReached;
				this.bestValue = bestValue; 
			}
		}

		///////////////////////////
		
		OutputResults( String competitorName, String datetime, 
				String problemClassName, ProblemClass problemClass, long trainingWallClockUsage, long testingWallClockUsage ) {

			this.competitorName = competitorName;
			this.problemClassName = problemClassName; 
			this.datetime = datetime; 
			Diag.println( problemClass );
			Diag.println( problemClass.getTrainingCategory() );			
			this.trainingCategory = problemClass.getTrainingCategory();
			for( ObjectiveFn o : problemClass.getTrainingInstances() ) {
				Pair< Long, Double > p = o.getRemainingEvaluationsAtBestValue();
				this.trainingResults.add( new Result( o.getRemainingEvaluations(), p.getLeft(), p.getRight() ) );
			}
			this.trainingWallClockUsage = trainingWallClockUsage; 		
			
			for( ObjectiveFn o : problemClass.getTestingInstances() ) {
				Pair< Long, Double > p = o.getRemainingEvaluationsAtBestValue();
				this.testingResults.add( new Result( o.getRemainingEvaluations(), p.getLeft(), p.getRight() ) );
			}
			this.testingWallClockUsage = testingWallClockUsage;			
		}

		///////////////////////////
		
		public String toJSonString() {
			Gson gson = new Gson();
			String result = gson.toJson( this );
			result = result.replaceAll( ",", ",\n\t" );
			return result;
		}
	}
	
	////////////////////////////////	
	
	public static void run( Competitor client ) throws IOException {
		
		String problemClassName;
		
		// read in root for problem class from classFolder.txt
		BufferedReader reader = null;
		String path = null; 
		try {
			String problemClassFile = System.getProperty( "user.dir" ) + "/resources/classFolder.txt";
			reader = new BufferedReader( new FileReader( problemClassFile ) );
			problemClassName = reader.readLine();
			path = System.getProperty( "user.dir" ) + "/resources/" + problemClassName;
		}
		finally {
			if( reader != null )
				reader.close();
		}
	    
		// String relativePathToProblem = "/resources/test/toy/";
		// String path = root + "/resources/test";
		// String path = root + relativePathToProblem;
		ProblemClass problemClass = new ProblemClass( Paths.get( path ), client.getTrainingCategory() );
		
		long actualTrainingTime = 0; 
		long actualTestingTime = 0; 
				
		switch( client.getTrainingCategory() ) {
			case NONE : {
				actualTestingTime = testClient( client, problemClass.getTestingInstances() );
				LOGGER.info( "actualTestingTime:" + actualTestingTime );				
			} break;
			case SHORT :
			case LONG : {				
				actualTrainingTime = trainClient( client, problemClass.getTrainingInstances() );
				LOGGER.info( "actualTrainingTime:" + actualTrainingTime );
				
				actualTestingTime = testClient( client, problemClass.getTestingInstances() );
				LOGGER.info( "actualTestingTime:" + actualTestingTime );				
			} break;
			default : 
				throw new IllegalStateException();
		}

		///////////////////////////

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String timestamp = dateFormat.format(new Date()); 
		OutputResults results = new OutputResults( client.getClass().getName(), 
				timestamp, problemClassName, problemClass, actualTrainingTime, actualTestingTime );
		
		String outputPath = path + "/results/" + "CBBOC2016results-" + client.getClass().getCanonicalName() + "-" + problemClassName + "-" + timestamp + ".json";
		PrintWriter pw = new PrintWriter( new FileOutputStream( new File( outputPath ) ) );
		pw.println( results.toJSonString() );
		pw.close();
		
		///////////////////////////		
		
		System.out.println( results.toJSonString() );
	}
}

// End ///////////////////////////////////////////////////////////////

package cbboc.examples;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import cbboc.CBBOC2015;
import cbboc.Competitor;
import cbboc.ObjectiveFn;
import cbboc.ProblemClass;
import cbboc.TrainingCategory;
import cbboc.util.RNG;

//////////////////////////////////////////////////////////////////////

public final class RandomCompetitor extends Competitor {

	public RandomCompetitor() {
		super( TrainingCategory.NONE );
	}

	@Override
	public void train(List<ObjectiveFn> trainingSet, long maxTimeInMilliseconds ) {
		// no training because we're in TrainingCategory.NONE 
		throw new UnsupportedOperationException();
	}

	@Override
	public void test( ObjectiveFn testCase, long maxTimeInMilliseconds ) {

		final long startTime = System.currentTimeMillis();
		while( true ) {
			// final long elapsed = System.currentTimeMillis() - startTime;
			// if( elapsed > maxTimeInMilliseconds )
			//	break;
			// time budget reached when elapsed > maxTimeInMilliseconds 
			// could check as above, but loop will be terminated automatically 
			// when time or evaluation budget is exceeded... 
			
			final boolean [] candidate = randomBitvector( testCase.getNumGenes() );
			final double value = testCase.value( candidate );
			// Useful strategies will obviously care about value...
		}
	}

	///////////////////////////////
	
	private static boolean [] randomBitvector( int length ) {
		boolean [] result = new boolean [ length ];
		for( int i=0; i<length; ++i )
			result[ i ] = RNG.get().nextBoolean();
		
		return result;
	}
	
	///////////////////////////////	

	public static void main( String [] args ) throws IOException {

		Competitor competitor = new RandomCompetitor();
		CBBOC2015.run( competitor );
	}
}

// End ///////////////////////////////////////////////////////////////

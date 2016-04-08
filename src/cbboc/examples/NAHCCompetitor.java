package cbboc.examples;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import cbboc.CBBOC;
import cbboc.Competitor;
import cbboc.ObjectiveFn;
import cbboc.ProblemClass;
import cbboc.TrainingCategory;
import cbboc.util.RNG;

//////////////////////////////////////////////////////////////////////

/**
 * Baseline metaheuristic strategy: Next Ascent Hillclimbing.  
 *
 */

public final class NAHCCompetitor extends Competitor {

	public NAHCCompetitor() {
		super( TrainingCategory.NONE );
	}

	@Override
	public void train(List<ObjectiveFn> trainingSet, long maxTimeInMilliseconds ) {
		// no training because we're in TrainingCategory.NONE 
		throw new UnsupportedOperationException();
	}

	///////////////////////////////	
	
	@Override
	public void test( ObjectiveFn testCase, long maxTimeInMilliseconds ) {

		boolean [] incumbent = randomBitvector( testCase.getNumGenes() );
		double bestValue = testCase.value( incumbent ); 
		boolean improved = false;
		do {
			List< boolean [] > neighbors = hamming1Neighbours( incumbent );
			for( boolean [] neighbor : neighbors ) {
				final double value = testCase.value( neighbor );
				if( value > bestValue ) {
					improved = true;
					incumbent = neighbor;
					bestValue = value;
				}
			}
			
		} while( improved && testCase.getRemainingEvaluations() > 0 );
	}

	///////////////////////////////
	
	private static List< boolean [] > hamming1Neighbours( boolean [] incumbent ) {
		List< boolean [] > result = new ArrayList< boolean [] >();
		for( int i=0; i<incumbent.length; ++i ) {
			boolean [] neighbour = incumbent.clone();
			neighbour[ i ] = !neighbour[ i ];
			result.add( neighbour );
		}
		
		return result;
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

		Competitor competitor = new NAHCCompetitor();
		CBBOC.run( competitor );
		
		System.out.println( "All done." );
	}
}

// End ///////////////////////////////////////////////////////////////

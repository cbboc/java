package cbboc.examples;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jeep.lang.Diag;
import jeep.math.ClosedInterval;
import jeep.math.LinearInterpolation;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.StatUtils;

import cbboc.CBBOC2015;
import cbboc.Competitor;
import cbboc.ObjectiveFn;
import cbboc.ProblemClass;
import cbboc.TrainingCategory;
import cbboc.util.RNG;

//////////////////////////////////////////////////////////////////////

/**
 * Baseline learning strategy: Simulated Annealing hyper-heuristic 
 * which learns the start and end values for the annealing schedule.  
 */

public final class SAHHCompetitor extends Competitor {

	private double saScheduleLowerBound = 0.0;
	private double saScheduleUpperBound = Double.MAX_VALUE;	
	
	///////////////////////////////
	
	public SAHHCompetitor( TrainingCategory trainingCategory ) {
		super( trainingCategory );
		assert( invariant() );
	}

	///////////////////////////////
	
	private static Pair< Double, Double > 
	WhiteTemperatureRangeForSA( List< Double > fitnessTrajectory ) {
		if( fitnessTrajectory.isEmpty() )
			throw new IllegalArgumentException();
		
		/**
		 * @see:
		 * @inproceedings{white:1984,
		 *  address = {Port Chester, NY},
		 *  author = {White, S. R.},
		 *  booktitle = {Proceeedings of the IEEE International Conference on Computer Design (ICCD) '84},
		 *  pages = {646--651},
		 *  title = {Concepts of Scale in Simulated Annealing},
		 *  year = {1984}
		 * }
		 */		
		Double minDifference = null;
		double [] asArray = new double [ fitnessTrajectory.size() ];
		asArray[ 0 ] = fitnessTrajectory.get( 0 );
		for( int i=1; i<fitnessTrajectory.size(); ++i )	{
			asArray[ i ] = fitnessTrajectory.get( i );
			
			final double delta = Math.abs( fitnessTrajectory.get( i ) 
					- fitnessTrajectory.get(  i - 1 ) );
			if( minDifference == null || delta < minDifference )
				minDifference = delta;
		}

		final double variance = StatUtils.variance( asArray );
		return Pair.of( minDifference, Math.sqrt( variance ) );		
	}
	
	///////////////////////////////

	private static boolean [] randomHamming1Neighbour( boolean [] incumbent ) {
		boolean [] neighbour = incumbent.clone();
		final int randomIndex = RNG.get().nextInt( neighbour.length ); 
		neighbour[ randomIndex ] = !neighbour[ randomIndex ];
		return neighbour;
	}
	
	///////////////////////////////
	
	private static List< Double > 
	fitnessTrajectoryOfRandomWalk( ObjectiveFn f, long numSteps ) {
		
		boolean [] incumbent = randomBitvector( f.getNumGenes() );
		
		List< Double > result = new ArrayList< Double >();
		for( int i=0; i<numSteps; ++i ) {
			boolean [] incoming = randomHamming1Neighbour( incumbent );
			result.add( f.value( incoming ) );

			incumbent = incoming;
		}
		return result;
	}
	
	///////////////////////////////	
	
	@Override
	public void train(List<ObjectiveFn> trainingSet, long maxTimeInMilliseconds ) {
		
		final long evalPerCase = trainingSet.get( 0 ).getRemainingEvaluations() / trainingSet.size();
		
		// ^ `remaining evaluations' for training are shared across all instances.
//		int totalEvaluations = 0;
//		for( int i=0; i<trainingSet.size(); ++i )
//			totalEvaluations += trainingSet.get( i ).getRemainingEvaluations();

		double [] saScheduleLowerBounds = new double [ trainingSet.size()];		
		double [] saScheduleUpperBounds = new double [ trainingSet.size()];
		for( int i=0; i<trainingSet.size(); ++i ) {
			Pair< Double, Double > saScheduleBounds = WhiteTemperatureRangeForSA( fitnessTrajectoryOfRandomWalk( trainingSet.get( i ), evalPerCase ) );
			saScheduleLowerBounds[ i ] = saScheduleBounds.getLeft();			
			saScheduleUpperBounds[ i ] = saScheduleBounds.getRight();			
		}
		
		saScheduleLowerBound = StatUtils.mean( saScheduleLowerBounds );
		saScheduleUpperBound = StatUtils.mean( saScheduleUpperBounds );		
		assert( invariant() );		
	}

	///////////////////////////////
	
	static double minDiffDivT = Double.POSITIVE_INFINITY;
	static double maxDiffDivT = 0.0;	
	
	private static boolean SAAccept( double lastValue, double currentValue, double temperature ) {
		if( Double.isNaN( temperature ) || temperature < 0 )
			throw new IllegalArgumentException( "Expected non-negative temperature, found:" + temperature );

		// assumes maximising...
		if( currentValue > lastValue )
			return true;			
		else if( temperature == 0 )
			return currentValue >= lastValue;
		else {
			assert currentValue <= lastValue;
			assert temperature > 0;
			
			final double diffDivT = ( currentValue - lastValue ) / temperature;
			// assert( diffDivT >= 0 );
			
			if( diffDivT < minDiffDivT )
				minDiffDivT = diffDivT; 
			if( diffDivT > maxDiffDivT )
				maxDiffDivT = diffDivT; 
			
			final double p = Math.exp( diffDivT );
			assert !Double.isNaN( p );
			return RNG.get().nextDouble() < p;
		}
	}	
	
	///////////////////////////////	
	
	@Override
	public void test( ObjectiveFn testCase, long maxTimeInMilliseconds ) {

		boolean [] incumbent = randomBitvector( testCase.getNumGenes() );
		double lastValue = testCase.value( incumbent ); 
		
		final long numEvaluations = testCase.getRemainingEvaluations();
		for( int i=0; i<numEvaluations; ++i ){
			
			final boolean [] incoming = randomHamming1Neighbour( incumbent );
			final double value = testCase.value( incoming );
			
			// linear annealing schedule...
			final double temperature = LinearInterpolation.apply( 
					i, 0, numEvaluations - 1, 
					saScheduleUpperBound, saScheduleLowerBound ); 
			if( SAAccept( lastValue, value, temperature ) ) {
				incumbent = incoming;
				lastValue = value;
			}
		}
	}

	///////////////////////////////

	private static boolean [] randomBitvector( int length ) {
		boolean [] result = new boolean [ length ];
		for( int i=0; i<length; ++i )
			result[ i ] = RNG.get().nextBoolean();
		
		return result;
	}
	
	public boolean invariant() {
		return saScheduleLowerBound < saScheduleUpperBound; 
	}
	
	///////////////////////////////	

	public static void main( String [] args ) throws IOException {
		
		Competitor competitor = new SAHHCompetitor( TrainingCategory.SHORT );
		CBBOC2015.run( competitor );

		Diag.println( "minDiffDivT: " + minDiffDivT + "exp:" + Math.exp( minDiffDivT ) );
		Diag.println( "maxDiffDivT:" + maxDiffDivT + "exp:" + Math.exp( maxDiffDivT ) );	

		
		System.out.println( "All done." );
	}
}

// End ///////////////////////////////////////////////////////////////

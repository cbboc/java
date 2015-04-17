package cbboc;

import jeep.lang.Diag;

import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.tuple.Pair;

public final class ObjectiveFn {
	
	private final ProblemInstance instance;
	private final TimingMode timingMode;
	private MutableLong remainingEvaluations;
	
	private Pair< Long, Double > remainingEvaluationsAtBestValue = null; // Pair.of( -1L, Double.NaN );
	
	///////////////////////////////
	
	enum TimingMode {
		TRAINING, TESTING
	};
	
	public ObjectiveFn( ProblemInstance instance, TimingMode timingMode, MutableLong remainingEvaluations ) { 

		this.instance = instance;
		this.timingMode = timingMode;
		// this.remainingEvaluations = instance.getMaxEvalsPerInstance();
		this.remainingEvaluations = remainingEvaluations;
	}

	///////////////////////////////
	
	public double value( boolean [] candidate ) {
		
		final long timeNow = System.currentTimeMillis();
		if( timingMode == TimingMode.TRAINING ) {
			if( timeNow > CBBOC2015.trainingEndTime )
				throw new CBBOC2015.TimeExceededException();				
		}
		else if( timingMode == TimingMode.TESTING ) {
			if( timeNow > CBBOC2015.testingEndTime )
				throw new CBBOC2015.TimeExceededException();
		}
		else {
			throw new IllegalStateException();
		}
		
		///////////////////////////
		
		if( remainingEvaluations.getValue() <= 0 ) {
			throw new CBBOC2015.EvaluationsExceededException();
		}
		else {
			final double value = instance.value( candidate );
			remainingEvaluations.setValue( remainingEvaluations.getValue() - 1 );
			
			// We are maximizing...
			if( remainingEvaluationsAtBestValue == null || value > remainingEvaluationsAtBestValue.getRight() )
				remainingEvaluationsAtBestValue = Pair.of( getRemainingEvaluations(), value );

			return value;
		}
	}
	
	///////////////////////////////

	public Pair< Long, Double > getRemainingEvaluationsAtBestValue() {
		if( remainingEvaluationsAtBestValue == null )
			return Pair.of( -1L, -1.0 );
		else
			return remainingEvaluationsAtBestValue;
	}
	
	public int getNumGenes() { return instance.getNumGenes(); }	
	public long getRemainingEvaluations() { return remainingEvaluations.getValue(); }
	public long getMaxEvalsPerInstance() { return instance.getMaxEvalsPerInstance(); }	
	
	///////////////////////////////	
	
	@Override
	public String toString() {
		// return ToStringBuilder.reflectionToString( this );
		String result = "ObjectiveFn(numGenes:" + getNumGenes(); 
		result += ",remainingEvaluations: " + getRemainingEvaluations();
		if( remainingEvaluationsAtBestValue == null ) {
			result += ",remainingEvaluationsAtBestValue: -1"; 
			result += ",bestValue: -1.0";
		}
		// if( remainingEvaluationsAtBestValue != null ) 
		else {
			result += ",remainingEvaluationsAtBestValue: " + remainingEvaluationsAtBestValue.getLeft(); 
			result += ",bestValue: " + remainingEvaluationsAtBestValue.getRight();
		}
		result += ",timingMode: " + timingMode + ")";
		return result;
	}
}

// End ///////////////////////////////////////////////////////////////

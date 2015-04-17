package cbboc;

import java.util.List;

public abstract class Competitor {

	private final TrainingCategory trainingCategory;
	
	///////////////////////////////	
	
	public Competitor( TrainingCategory trainingCategory ) {
		this.trainingCategory = trainingCategory;
	}
	
	///////////////////////////////
	
	public final TrainingCategory getTrainingCategory() { return trainingCategory; }
	
	public abstract void train( List< ObjectiveFn > trainingSet, long maxTimeInMilliseconds );
	
	public abstract void test( ObjectiveFn testCase, long maxTimeInMilliseconds );	
}

// End ///////////////////////////////////////////////////////////////


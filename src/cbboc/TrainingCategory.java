package cbboc;

public enum TrainingCategory {

	NONE( 0 ), SHORT( 1 ), LONG( 10 );

	///////////////////////////////
	
	private final int multiplier;
	
	private TrainingCategory( int multiplier ) {
		this.multiplier = multiplier;
	}
	
	public final long getMultiplier() {
		return multiplier;
	}
}

// End ///////////////////////////////////////////////////////////////

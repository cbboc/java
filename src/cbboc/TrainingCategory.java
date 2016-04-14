package cbboc;

public enum TrainingCategory {

	NONE( 0, 1 ), SHORT( 1, 1 ), LONG( 10, 2 );

	///////////////////////////////
	
	private final int multiplier, numericCode;
	
	private TrainingCategory( int multiplier, int numericCode ) {
		this.multiplier = multiplier;
		this.numericCode = numericCode;
	}
	
	public final long getMultiplier() { return multiplier; }
	public final int getNumericCode() { return numericCode; }	
}

// End ///////////////////////////////////////////////////////////////

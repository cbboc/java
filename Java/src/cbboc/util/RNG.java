package cbboc.util;

import java.util.Random;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;

public final class RNG {
	
	private static Random instance = new RandomAdaptor( new MersenneTwister() );
	// ^ MersenneTwister is a much better quality RNG than the standard Java one.
	
	///////////////////////////////
	
	public static Random get() { return instance; }
}

// End ///////////////////////////////////////////////////////////////


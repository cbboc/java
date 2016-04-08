package cbboc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.tuple.Pair;

//////////////////////////////////////////////////////////////////////

public final class ProblemInstance {

	private int numGenes;
	private int maxEvalsPerInstance;
	private int K;

	// Change for 2016:
	// Instead of each file having N+1 rows (header and N functions), 
	// where N is the number of problem variables, each now has M+1 rows, 
	// with the value of M added to the end of the header.
	private int M;
	
	private List< Pair< int [], double [] > 
		> data = new ArrayList< Pair< int [], double [] > >();
	
	///////////////////////////////
	
	public ProblemInstance( InputStream is ) throws IOException {
		LineNumberReader r = new LineNumberReader( 
				new BufferedReader( new InputStreamReader( is ) ) );
			
		Scanner s = null;
		
		try {
			String line = r.readLine();
			s = new Scanner( line );

			numGenes = s.nextInt();
			maxEvalsPerInstance = s.nextInt();

			// Previously the number of variables in each row was given by the third value in the header, such that if that value was K, each row had K+1 variables. I've changed this such that the third value now directly says how many variables are in each row.
			// K = s.nextInt();
			K = s.nextInt() - 1;
			
			M = s.nextInt();			
		
			// final int numRows = numGenes;
			final int numRows = M;
			// for( int i=0; i<numGenes; ++i ) {
			for( int i=0; i<numRows; ++i ) {			
				line = r.readLine();
				
				s = new Scanner( line );
				
				int [] iarray = new int [ K + 1 ];
				for( int j=0; j<K + 1; ++j )
					iarray[ j ] = s.nextInt();

				final int numFks = 1 << ( K + 1 );
				double [] darray = new double [ numFks ];
				for( int j=0; j<numFks; ++j )
					darray[ j ] = s.nextDouble();
				
				data.add( Pair.of( iarray, darray ) );
			}
		}
		finally {
			if( s != null )
				s.close();
		}
		
		assert( invariant() );
	}
	
	///////////////////////////////

	public int getNumGenes() { return numGenes;	}
	public int getMaxEvalsPerInstance() { return maxEvalsPerInstance; }

	///////////////////////////////
	
	double value( boolean [] candidate ) {
		if( candidate.length != getNumGenes() )
			throw new IllegalArgumentException( "candidate of length " + getNumGenes() + " expected, found " + candidate.length );
		
		double total = 0.0;
		for( int i=0; i<getNumGenes(); ++i ) {
			int [] varIndices = data.get( i ).getLeft();
			int fnTableIndex = 0;
			for( int j=0; j<varIndices.length; ++j ) {
				fnTableIndex <<= 1;
				fnTableIndex |= candidate[ varIndices[ j ] ] ? 1 : 0;
			}
			
			total += data.get( i ).getRight()[ fnTableIndex ];
		}
		
		return total;
	}
	
	///////////////////////////////	

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append( "ProblemInstance(" );		
		result.append( "numGenes=" + numGenes );
		result.append( ",maxEvalsPerInstance=" + maxEvalsPerInstance );
		result.append( ")" );		
		return result.toString();
	}
	
	///////////////////////////////	
	
	String debugToString() {
		StringBuffer result = new StringBuffer();
		result.append( "ProblemInstance[" );		
		result.append( "numGenes=" + numGenes );
		result.append( ",maxEvalsPerInstance=" + maxEvalsPerInstance );
		result.append( ",K=" + K );
		
		result.append( ",data=[\n" );		
		for( int i=0; i<data.size(); ++i ) {
			result.append( "(" + Arrays.toString( data.get( i ).getLeft() ) );
			result.append( "," + Arrays.toString( data.get( i ).getRight() ) );
			result.append( ")\n" );			
		}
		result.append( "]]" );	
		return result.toString();
	}
	
	///////////////////////////////
	
	private static boolean allValidSize( List< Pair< int [], double [] > > data, int k ) {
		for( Pair< int [], double [] > p : data )
			if( p.getLeft().length != k + 1 || p.getRight().length != 1 << ( k + 1 ) )
				return false;
		
		return true;
	}
	
	public boolean invariant() {
		return getNumGenes() > 0 && 
			getMaxEvalsPerInstance() > 0 &&
			K > 0 && 
			data.size() == getNumGenes() &&
			allValidSize( data, K );
	}

	///////////////////////////////	
	
	public static void main( String [] args ) throws IOException {
		String root = System.getProperty( "user.dir" );
		// String path = root + "/resources/" + "00000.txt";
		String path = root + "/resources/" + "toy.txt";
		
		InputStream is = new FileInputStream( path );
	
		ProblemInstance prob = new ProblemInstance( is );
		System.out.println( prob );
		
		boolean [] candidate = new boolean [ prob.getNumGenes() ];
		System.out.println( prob.value( candidate ) );
		
		System.out.println( "All done" );		
	}
}

// End ///////////////////////////////////////////////////////////////

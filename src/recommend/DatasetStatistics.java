package recommend;

import org.json.*;

import recommend.util.WordIndex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class DatasetStatistics {
	static final String DATASET = "movielens-all.json";
	
	public static void main( String[] args ) throws Throwable {
		System.out.println( "Dataset: " + DATASET );
		BufferedReader in = new BufferedReader( new FileReader( DATASET ) );
        JSONObject json = new JSONObject( in.readLine() );

        int ndocs = json.getInt( "ndocs" );
        HashMap<Integer, Double>[] docs = new HashMap[ndocs];
        JSONArray arr = json.getJSONArray( "docs" );

        for( int i = 0; i < ndocs; i++ ) {
            JSONObject doc = arr.getJSONObject( i );
            docs[i] = new HashMap<Integer,Double>();
            JSONArray terms = doc.getJSONArray( "terms" );

            for( int j = 0; j < terms.length(); j++ ) {
                String term = terms.getString( j );
                WordIndex.add( terms.getString( j ) );
                int index = WordIndex.indexOf( term );
                WordIndex.incrementDF( index );
                docs[i].put( index, 1.0 );
            }
        }
        
        for( int w = 0; w < WordIndex.size(); w++ ) {
            WordIndex.setIDF( w, Math.log( (double)ndocs / WordIndex.getDF( w ) ) );
        }
        
        int nterms = WordIndex.size();
        
        System.out.println( "--------\nNumber of documents: " + ndocs );
        System.out.println( "Number of terms: " + nterms );
        
        System.out.println( "--------\nDocument Length" );
        double[] doclengths = new double[ndocs];
        
        for( int i = 0; i < ndocs; i++ ) {
        	doclengths[i] = docs[i].size();
        }
        
        System.out.println( "Mean: " + mean( doclengths ) );
        System.out.println( "Median: " + median( doclengths ) );
        System.out.println( "SD: " + sd( doclengths ) );
        System.out.println( "Min: " + min( doclengths ) );
        System.out.println( "Max: " + max( doclengths ) );
        
        System.out.println( "--------\nTerms" );
        double[] termlengths = new double[nterms];
        
        for( int i = 0; i < nterms; i++ ) {
        	termlengths[i] = WordIndex.getDF( i );
        }

        System.out.println( "Mean: " + mean( termlengths ) );
        System.out.println( "Median: " + median( termlengths ) );
        System.out.println( "SD: " + sd( termlengths ) );
        System.out.println( "Min: " + min( termlengths ) );
        System.out.println( "Max: " + max( termlengths ) );
        
        System.out.println( "--------\nTop 1% Terms" );
        PriorityQueue<Pair> pq = new PriorityQueue<Pair>();
        
        for( int i = 0; i < nterms; i++ ) {
        	if( pq.size() <= 0.01*nterms ) {
        		pq.add( new Pair( i, termlengths[i] ) );
        	} else if( termlengths[i] > pq.peek().b ) {
        		pq.poll();
        		pq.add( new Pair( i, termlengths[i] ) );
        	}
        }
        
        while( !pq.isEmpty() ) {
        	Pair p = pq.poll();
        	System.out.println( (int)p.b + "\t" + WordIndex.get( p.a ) );
        }
    }
	
	public static double mean( double[] a ) {
		double sum = 0.0;
		
		for( double x : a ) {
			sum += x;
		}
		
		return sum / a.length;
	}
	
	public static double sd( double[] a ) {
		double mean = mean( a );
		double sum = 0.0;
		
		for( double x : a ) {
			sum += ( x - mean )*( x - mean );
		}
		
		return Math.sqrt( sum/a.length );
	}
	
	public static double min( double[] a ) {
		double min = Double.MAX_VALUE;
		
		for( double x : a ) {
			if( x < min ) {
				min = x;
			}
		}
		
		return min;
	}
	
	public static double max( double[] a ) {
		double max = Double.MIN_VALUE;
		
		for( double x : a ) {
			if( x > max ) {
				max = x;
			}
		}
		
		return max;
	}
	
	public static double median( double[] a ) {
		a = Arrays.copyOf( a, a.length );
		
		if( a.length % 2 == 0 ) {
			return ( a[a.length/2-1]+a[a.length/2] )/2;
		} else {
			return a[a.length/2];
		}
	}
	
	static class Pair implements Comparable<Pair> {
		int a;
		double b;
		
		public Pair( int a, double b ) {
			this.a = a;
			this.b = b;
		}
		
		public int compareTo( Pair o ) {
			return b > o.b ? 1 : -1;
		}
	}
}

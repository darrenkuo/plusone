package recommend;

import java.io.*;
import java.util.*;
import org.json.*;

import util.WordIndex;
import recommend.algorithms.*;

public class Main {
	//static final String FILENAME = "alldata_bf_sample20000d.out";
	//static final String FILENAME = "test.5000.data";
	//static final String FILENAME = "movielens-pos.json";
	static final String FILENAME = "data/med.json"; 
	static final Algorithm[] algs = new Algorithm[] {
            //new SVD( 50 ),
            //new SVD( 100 ),
		//new KNN(5)
		/*
		new LSIWKNN( 5, 5 ),
		new LSIWKNN( 5, 10 ),
		new LSIWKNN( 5, 25 ),
		new LSIWKNN( 5, 50 ),
		new LSIWKNN( 10, 5 ),
		new LSIWKNN( 10, 10 ),
		new LSIWKNN( 10, 25 ),
		new LSIWKNN( 10, 50 ),
		new LSIWKNN( 25, 5 ),
		new LSIWKNN( 25, 10 ),
		new LSIWKNN( 25, 25 ),
		new LSIWKNN( 25, 50 ),
		new LSIWKNN( 50, 5 ),
		*/
		//new LSIWKNN( 50, 10 ),
		//new LSIWKNN( 50, 25 ),
		//new LSIWKNN( 5, 1000 ),
		//new LSI( 5 )
		new Baseline(),
		/*
		new KNN( 1 ),
		new KNN( 5 ),
		new KNN( 10 ),
		new KNN( 15 ),
		new KNN( 25 ),
		new KNN( 50 ),
		new KNN( 100 ),
		new KNN( 200 ),
		new KNN( 500 ),
		new KNN( 1000 ),
		new WKNN( 1 ),
		new WKNN( 5 ),
		new WKNN( 10 ),
		new WKNN( 15 ),
		new WKNN( 25 ),
		new WKNN( 50 ),
		new WKNN( 100 ),
		new WKNN( 200 ),
		new WKNN( 500 ),
		new WKNN( 1000 ),
		new LSI( 1 ),
		new LSI( 5 ),
		new LSI( 10 ),
		new LSI( 15 ),
		new LSI( 25 ),
		new LSI( 50 ),
		new LSI( 100 ),
		*/
		/*new CooccurSum(),
		new CooccurSumIDF(),
		new CooccurMax(),*/
	};
	static final Random rand = new Random(0);
	
	static double trainPercent = 0.8;
	static double testPercent = 0.5;
	static int RUNS = 5;
	static int NUM_PRED = 1;
	
	static int ndocs;
	static HashMap<Integer,Double>[] docs;
	
	public static void main( String[] args ) throws Throwable {
		System.out.println( "File: " + FILENAME );
		BufferedReader in = new BufferedReader( new FileReader( FILENAME ) );
		JSONObject json = new JSONObject( in.readLine() );
		
		ndocs = json.getInt( "ndocs" );
		docs = new HashMap[ndocs];
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
		
		for( Algorithm alg : algs ) {
			System.out.print( alg.name + "\t" );
			double total = 0.0;
			
			for( int run = 0; run < RUNS; run++ ) {
				ArrayList<HashMap<Integer,Double>> traindocs = new ArrayList<HashMap<Integer,Double>>();
				ArrayList<HashMap<Integer,Double>> testdocs = new ArrayList<HashMap<Integer,Double>>();
				
				for( HashMap<Integer,Double> doc : docs ) {
					if( rand.nextDouble() < trainPercent )
						traindocs.add( doc );
					else
						testdocs.add( doc );
				}
				
				alg.train( traindocs );
				int successes = 0;
				
				for( HashMap<Integer,Double> testdoc : testdocs ) {
					HashMap<Integer,Double> givenwords = new HashMap<Integer,Double>();
					HashSet<Integer> testwords = new HashSet<Integer>();
					
					for( int word : testdoc.keySet() ) {
						if( rand.nextDouble() < testPercent ) {
							testwords.add( word );
						} else {
							givenwords.put( word, testdoc.get( word ) );
						}
					}
					
					double[] scores = alg.predict( givenwords );
			    	PriorityQueue<Pair> pq = new PriorityQueue<Pair>();
					
					for( int i = 0; i < scores.length; i++ ) {
						if( givenwords.containsKey( i ) ) {
							continue;
						}
							
						if( pq.size() < NUM_PRED ) {
							pq.add( new Pair( i, scores[i] ) );
						} if( scores[i] > pq.peek().score ) {
							pq.poll();
							pq.add( new Pair( i, scores[i] ) );
						}
					}
					
					while( !pq.isEmpty() ) {
						Pair pair = pq.poll();
						//System.out.println( WordIndex.get( pair.word ) + "\t" + pair.score + "\t" + testwords.contains( pair.word ) );
						if( testwords.contains( pair.word ) ) {
							successes++;
						}
					}
				}
				
				total += (double)successes/NUM_PRED/testdocs.size();
			}
			
			System.out.println( total/RUNS );
	    }
	}
	
	private static class Pair implements Comparable<Pair> {
		int word;
		double score;
		
		public Pair( int word, double score ) {
			this.word = word;
			this.score = score;
		}
		
		public int compareTo( Pair p ) {
			return score > p.score ? 1 : -1;
		}
	}
}

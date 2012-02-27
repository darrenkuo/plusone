package recommend;

import java.io.*;
import java.util.*;

import org.json.*;

import util.WordIndex;

import recommend.algorithms.*;

public class MainDebug {
	//static final String FILENAME = "alldata_bf_sample20000d.out";
	//static final String FILENAME = "test.5000.data";
	static final String FILENAME = "med.json";
	static final Algorithm[] algs = new Algorithm[] {
		//new CooccurSum(),
		//new CooccurMax(),
		//new CooccurAvgIDF(),
		//new CooccurSumIDF(),
		//new CooccurSum2(),
		//new Cooccur2SumIDF(),
		//new Cooccur2Max()
		//new KNN(50),
		//new NewAlg2()
		//new KNN(25)
		/*new WKNN(1),
		new WKNN(5),
		new WKNN(10),
		new WKNN(25),
		new WKNN(50),
		new WKNN(100),*/
		//new WKNN(200),
		//new WKNN(500),
		new Baseline()
	};
	static final Random rand = new Random(0);
	
	static double trainPercent = 0.8;
	static double testPercent = 0.5;
	static int RUNS = 1;
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
						System.out.println(WordIndex.get( pair.word ) + "\t" + pair.score + "\t" +testwords.contains( pair.word ) );
						
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

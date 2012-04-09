package recommend;

import java.io.*;
import java.util.*;
import org.json.*;

import recommend.algorithms.*;
import recommend.util.*;


public class MainOld {
	static String DATASET;
	static final Algorithm[] algs = new Algorithm[] {
		//new WeightedNeighbors(),
		//new StochasticRWWKNN(),
		//new WKNN( 25 )
		new ItemAverage(),
		new StochasticRWCooccurSum(),
		new StochasticRWWKNN(),
		//new StochasticRW( 1 )
		//new CooccurSum(),
		//new CooccurSumIDF(),
		//new Baseline(),
		/*
		new Baseline(),
		new KNN( 5 ),
		new KNN( 10 ),
		new KNN( 25 ),
		new KNN( 50 ),
		new KNN( 100 ),
		new WKNN( 5 ),
		new WKNN( 10 ),
		new WKNN( 25 ),
		new WKNN( 50 ),
		new WKNN( 100 ),
		new LSI( 1 ),
		new LSI( 5 ),
		new LSI( 10 ),
		new CooccurSum(),
		new CooccurSumIDF(),
		new CooccurMax(),
		*/
		/*
		new Baseline(),
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
		new LSI( 200 ),
		new StochasticRWCooccurSum(),
		new CooccurSum(),
		new CooccurSumIDF(),
		new CooccurMax(),
		*/
	};
	public static Random rand;
	
	static double TRAINPERCENT;
	static double TESTPERCENT;
	static int RUNS;
	static int PREDICTIONS;
	
	static int ndocs;
	static HashMap<Integer,Double>[] docs;
	
	public static void main( String[] args ) throws Throwable {
		DATASET = System.getProperty( "dataset", "movielens-pos.json" );
		TRAINPERCENT = Double.parseDouble( System.getProperty( "trainPercent", "0.8" ) );
		TESTPERCENT = Double.parseDouble( System.getProperty( "testPercent", "0.5" ) );
		RUNS = Integer.parseInt( System.getProperty( "runs", "1" ) );
		PREDICTIONS = Integer.parseInt( System.getProperty( "predictions", "1" ) );
		//rand = new Random( Integer.parseInt( System.getProperty( "seed", "1" ) ) );
		rand = new Random();
		
		System.out.println( "File: " + DATASET );
		System.out.println( "Train Percent: " + TRAINPERCENT );
		System.out.println( "Test Percent: " + TESTPERCENT );
		System.out.println( "Runs: " + RUNS );
		System.out.println( "Predictions: " + PREDICTIONS );
		docs = DatasetOld.loadDataset( DATASET );
		
		for( Algorithm alg : algs ) {
			System.out.print( alg.name + "\t" );
			double total = 0.0;
			
			long trainTime = 0;
			long predictTime = 0;
			
			for( int run = 0; run < RUNS; run++ ) {
				ArrayList<HashMap<Integer,Double>> traindocs = new ArrayList<HashMap<Integer,Double>>();
				ArrayList<HashMap<Integer,Double>> testdocs = new ArrayList<HashMap<Integer,Double>>();
				
				for( HashMap<Integer,Double> doc : docs ) {
					if( rand.nextDouble() < TRAINPERCENT )
						traindocs.add( doc );
					else
						testdocs.add( doc );
				}
				
				long startTime = System.nanoTime();
				alg.train( traindocs );
				trainTime += System.nanoTime()-startTime;
				
				int successes = 0;
				
				for( HashMap<Integer,Double> testdoc : testdocs ) {
					HashMap<Integer,Double> givenwords = new HashMap<Integer,Double>();
					HashSet<Integer> testwords = new HashSet<Integer>();
					
					for( int word : testdoc.keySet() ) {
						if( rand.nextDouble() < TESTPERCENT ) {
							testwords.add( word );
						} else {
							givenwords.put( word, testdoc.get( word ) );
						}
					}
					
					startTime = System.nanoTime();
					double[] scores = alg.predict( givenwords );
					predictTime += System.nanoTime()-startTime;
					//System.out.println(Arrays.toString( scores ));
					PriorityQueue<Pair> pq = new PriorityQueue<Pair>();
					
					for( int i = 0; i < scores.length; i++ ) {
						if( givenwords.containsKey( i ) ) {
							continue;
						}
							
						if( pq.size() < PREDICTIONS ) {
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
				
				total += (double)successes/PREDICTIONS/testdocs.size();
			}
			
			System.out.println( total/RUNS + "\t" + ( trainTime/1000000000.0/RUNS ) + "\t" + ( predictTime/1000000000.0/RUNS ) );
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


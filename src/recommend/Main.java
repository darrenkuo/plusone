package recommend;

import java.util.*;
import plusone.clustering.*;
import recommend.algorithms.*;
import recommend.util.*;

public class Main {
	static String DATASET;
	static final Algorithm[] algs = new Algorithm[] {
		//new ItemAverage(),
		//new StochasticRWCooccurSum(),
		//new StochasticRWWKNN(),
		//new StochasticRW( 1 )
		//new CooccurSum(),
		//new CooccurSumIDF(),
		//new recommend.algorithms.Baseline(null, null),
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
	
	static double TESTPERCENT;
	static int RUNS;
	static int PREDICTIONS;
	
	static int num_users;
	static HashMap<Integer,Double>[] users;
	
	public static void main( String[] args ) throws Throwable {
		DATASET = System.getProperty( "dataset", "reg_movielens5.json" );
		TESTPERCENT = Double.parseDouble( System.getProperty( "testPercent", "0.5" ) );
		RUNS = Integer.parseInt( System.getProperty( "runs", "5" ) );
		PREDICTIONS = Integer.parseInt( System.getProperty( "predictions", "1" ) );
		rand = System.getProperty( "seed" ) == null ? new Random() : new Random( Integer.parseInt( System.getProperty( "seed" ) ) );
		
		System.out.println( "File: " + DATASET );
		System.out.println( "Test Percent: " + TESTPERCENT );
		System.out.println( "Runs: " + RUNS );
		System.out.println( "Predictions: " + PREDICTIONS );
		Dataset dataset = new Dataset( DATASET );
		
		for( String term : dataset.itemindex ) {
			WordIndex.add( term );
		}
		
		for( Algorithm alg : algs ) {
			System.out.print( alg.name + "\t" );
			double total = 0.0;
			
			long trainTime = 0;
			long predictTime = 0;
			
			for( int run = 0; run < RUNS; run++ ) {
				ArrayList<HashMap<Integer,Double>> training = new ArrayList<HashMap<Integer,Double>>();
				ArrayList<HashMap<Integer,Double>> testing = new ArrayList<HashMap<Integer,Double>>();
				int testfold = RUNS >= dataset.num_folds ? run : rand.nextInt( dataset.num_folds );
				
				for( int i = 0; i < dataset.num_folds; i++ ) {
					if( i == testfold ) {
						for( HashMap<Integer,Double> doc : dataset.folds[i] ) {
							testing.add( doc );
						}
					} else {
						for( HashMap<Integer,Double> doc : dataset.folds[i] ) {
							training.add( doc );
						}
					}
				}
				
				long startTime = System.nanoTime();
				/*alg.train( training );
				trainTime += System.nanoTime()-startTime;*/
				
				int successes = 0;
				
				for( HashMap<Integer,Double> testuser : testing ) {
					HashMap<Integer,Double> givenitems = new HashMap<Integer,Double>();
					HashSet<Integer> testitems = new HashSet<Integer>();
					
					for( int word : testuser.keySet() ) {
						if( rand.nextDouble() < TESTPERCENT ) {
							testitems.add( word );
						} else {
							givenitems.put( word, testuser.get( word ) );
						}
					}
					
					startTime = System.nanoTime();
					double[] scores = alg.predict( givenitems );
					predictTime += System.nanoTime()-startTime;
					//System.out.println(Arrays.toString( scores ));
					PriorityQueue<Pair> pq = new PriorityQueue<Pair>();
					
					for( int i = 0; i < scores.length; i++ ) {
						if( givenitems.containsKey( i ) ) {
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
						//System.out.println( WordIndex.get( pair.item ) + "\t" + pair.score + "\t" + testitems.contains( pair.item ) );
						if( testitems.contains( pair.item ) ) {
							successes++;
						}
					}
				}
				
				total += (double)successes/PREDICTIONS/testing.size();
			}
			
			System.out.println( total/RUNS + "\t" + ( trainTime/1000000000.0/RUNS ) + "\t" + ( predictTime/1000000000.0/RUNS ) );
	    }
	}
	
	private static class Pair implements Comparable<Pair> {
		int item;
		double score;
		
		public Pair( int word, double score ) {
			this.item = word;
			this.score = score;
		}
		
		public int compareTo( Pair p ) {
			return score > p.score ? 1 : -1;
		}
	}
}


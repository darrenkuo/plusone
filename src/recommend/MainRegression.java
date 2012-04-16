package recommend;

import java.util.*;
import recommend.algorithms.*;
import recommend.util.*;

public class MainRegression {
	static String DATASET;
	static final Algorithm[] algs = new Algorithm[] {
		new ItemAverage(),
		new UserAverage(),
		
		/*new KNN(1),
		new KNN(2),
		new KNN(5),
		new KNN(10),
		new KNN(50),
		new KNN(100),
		new KNN(1000),
		*/
		
		/*new WKNN(1),
		new WKNN(2),
		new WKNN(5),
		new WKNN(10),
		new WKNN(50),
		new WKNN(100),
		new WKNN(1000),
		new WKNN(100000),*/
		//new WeightedNeighbors(),
	};
	public static Random rand;
	
	static double TESTPERCENT;
	static int RUNS;
	
	static int num_users;
	static HashMap<Integer,Double>[] users;
	
	public static void main( String[] args ) throws Throwable {
		DATASET = System.getProperty( "dataset", "reg_movielens5.json" );
		//DATASET = System.getProperty( "dataset", "reg_simple.json" );
		TESTPERCENT = Double.parseDouble( System.getProperty( "testPercent", "0.5" ) );
		RUNS = Integer.parseInt( System.getProperty( "runs", "1" ) );
		rand = System.getProperty( "seed" ) == null ? new Random() : new Random( Integer.parseInt( System.getProperty( "seed" ) ) );
		
		System.out.println( "File: " + DATASET );
		System.out.println( "Test Percent: " + TESTPERCENT );
		System.out.println( "Runs: " + RUNS );
		Dataset dataset = new Dataset( DATASET );
		
		for( String term : dataset.itemindex ) {
			WordIndex.add( term );
		}
		
		for( Algorithm alg : algs ) {
			System.out.print( alg.name + "\t" );
			double mae = 0.0;
			int tests = 0;
			
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
				alg.train( training );
				trainTime += System.nanoTime()-startTime;
				
				for( HashMap<Integer,Double> testuser : testing ) {
					HashMap<Integer,Double> givenitems = new HashMap<Integer,Double>();
					HashMap<Integer,Double> testitems = new HashMap<Integer,Double>();
					
					for( int word : testuser.keySet() ) {
						if( rand.nextDouble() < TESTPERCENT ) {
							testitems.put( word, testuser.get( word ) );
						} else {
							givenitems.put( word, testuser.get( word ) );
						}
					}
					
					startTime = System.nanoTime();
					double[] scores = alg.predict( givenitems );
					predictTime += System.nanoTime()-startTime;
					//System.out.println(Arrays.toString( scores ));
					
					for( int item : testitems.keySet() ) {
						mae += Math.abs( testitems.get( item )-scores[item] );
					}
					
					tests += testitems.size();
				}
			}
			
			System.out.println( mae/tests + "\t" + ( trainTime/1000000000.0/RUNS ) + "\t" + ( predictTime/1000000000.0/RUNS ) );
	    }
	}
}


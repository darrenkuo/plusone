package recommend;

import java.io.*;
import java.util.*;

import recommend.algorithms.Algorithm;
import recommend.algorithms.CooccurSumIDF;

public class Test2 {
	static final String FILENAME = "med.out";
	static final Algorithm[] algs = new Algorithm[] {
		new CooccurSumIDF()
	};
	static final Random rand = new Random( 3 );
	
	static double trainPercent = 0.8;
	static double testPercent = 0.5;
	static int RUNS = 1;
	static int NUM_PRED = 1;
	
	static ArrayList<HashMap<Integer,Double>> docs = new ArrayList<HashMap<Integer,Double>>();
	
	public static void main( String[] args ) throws Throwable {
		BufferedReader in = new BufferedReader( new FileReader( FILENAME ) );
		StringTokenizer st;
		
		while( in.readLine() != null ) {
			int id = Integer.parseInt( in.readLine().substring( 7 ) );
			st = new StringTokenizer( in.readLine().substring( 7 ) );
			int[] inref = new int[st.countTokens()];
			
			for( int i = 0; i < inref.length; i++ )
				inref[i] = Integer.parseInt( st.nextToken() );
			
			st = new StringTokenizer( in.readLine().substring( 8 ) );
			int[] outref = new int[st.countTokens()];
			
			for( int i = 0; i < outref.length; i++ )
				outref[i] = Integer.parseInt( st.nextToken() );
			
			st = new StringTokenizer( in.readLine().substring( 9 ) );
			HashMap<Integer,Double> words = new HashMap<Integer,Double>();
			
			while( st.hasMoreTokens() ) {
				String s = st.nextToken();
				WordIndex.add( s );
				int word = WordIndex.indexOf( s );
				
				if( words.containsKey( word ) ) {
					words.put( word, words.get( word )+1 );
				} else {
					words.put( word, 1.0 );
				}
			}
			
			docs.add( words );
			in.readLine();
		}
		
		double[] count = new double[WordIndex.size()];
		double[] idf = new double[WordIndex.size()];
		
		for( HashMap<Integer,Double> doc : docs ) {
			for( int word : doc.keySet() ) {
				count[word]++;
			}
		}
		
		for( int word = 0; word < idf.length; word++ ) {
			idf[word] = Math.log( (double)docs.size() / ( 1+count[word] ) );
		}
		
		int w1 = WordIndex.indexOf( "able" );
		int w2 = WordIndex.indexOf( "2" );
		
		System.out.println(idf[w1]);
		
		for( HashMap<Integer,Double> doc : docs ) {
			if( doc.containsKey( w1 ) ) {
				System.out.println( doc.containsKey( w2 ) );
			}
		}
	}
}

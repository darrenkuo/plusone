package recommend;

import java.io.*;
import java.util.*;

import util.WordIndex;

import algorithms.Algorithm;
import algorithms.CooccurSumIDF;

public class Test1 {
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
			//idf[word] = Math.log( (double)docs.size() / ( 1+count[word] ) );
			idf[word] = (double)docs.size() / ( 1+count[word] );
		}
		
		for( int i = 0; i < WordIndex.size(); i++ ) {
			double[] arr = new double[WordIndex.size()];
			
			for( HashMap<Integer,Double> doc : docs ) {
				if( doc.containsKey( i ) ) {
					for( int j : doc.keySet() ) {
						arr[j]++;
					}
				}
			}
			
			double sum = 0.0;
			
			for( int j = 0; j < arr.length; j++ ) {
				arr[j] = Math.abs( ( arr[j]/count[i] ) - ( count[j]/docs.size() ) )/( count[j]/docs.size() );
				
				if( count[j] > 1 ) {
					sum += arr[j];
				}
			}
			
			double avg = sum/arr.length;
			
			System.out.println( i + "\t" + WordIndex.get( i ) + "\t" + idf[i] + "\t" + avg  );
		}
	}
}

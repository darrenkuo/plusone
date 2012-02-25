
import java.util.*;

public final class WordIndex {
	static ArrayList<String> words = new ArrayList<String>();
	static HashMap<String,Integer> wordindex = new HashMap<String,Integer>();
	static HashMap<Integer,Integer> df = new HashMap<Integer,Integer>();
	static HashMap<Integer,Double> idf = new HashMap<Integer,Double>();
	
	public static boolean add( String item ) {
		if( contains( item ) )
			return false;
		
		words.add( item );
		wordindex.put( item, size()-1 );
		return true;
	}
	
	public static boolean contains( String item ) {
		return wordindex.containsKey( item );
	}
	
	public static int indexOf( String item ) {
		Integer index = wordindex.get( item );
		return index == null ? -1 : index;
	}
	
	public static String get( int index ) {
		return words.get( index );
	}
	
	public static void incrementDF( int index ) {
		if( df.containsKey( index ) ) {
			df.put( index, df.get( index )+1 );
		} else {
			df.put( index, 1 );
		}
	}
	
	public static double getDF( int index ) {
		return df.get( index );
	}
	
	public static void setIDF( int index, double v ) {
		idf.put( index, v );
	}
	
	public static double getIDF( int index ) {
		return idf.get( index );
	}
	
	public static int size() {
		return words.size();
	}
}

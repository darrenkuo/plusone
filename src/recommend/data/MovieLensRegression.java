package recommend.data;

import java.io.*;
import java.util.*;

import org.json.*;

public class MovieLensRegression {
	public static void main( String[] args ) throws Throwable {
	    Scanner in = new Scanner( new File( "ratings.dat" ) );
    	PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( "movielens-reg5.json" ) ) );
	    String s;
	    HashMap<Integer,HashMap<Integer,Double>> hm = new HashMap<Integer,HashMap<Integer,Double>>();
	    HashSet<Integer> movies = new HashSet<Integer>();
	    
	    while( in.hasNextLine() ) {
	    	String[] arr = in.nextLine().split( "::" );
	    	int user = Integer.parseInt( arr[0] );
	    	int movie = Integer.parseInt( arr[1] );
	    	int rating = Integer.parseInt( arr[2] );
	    	int time = Integer.parseInt( arr[3] );
	    	
	    	if( hm.containsKey( user ) ) {
	    		hm.get( user ).put( movie, (double)rating );
	    	} else {
	    		hm.put( user, new HashMap<Integer,Double>() );
	    		hm.get( user ).put( movie, (double)rating );
	    	}
	    	
	    	movies.add( movie );
	    }
	    
	    JSONObject json = new JSONObject();
    	json.put( "ndocs", hm.keySet().size() );
    	json.put( "nterms", movies.size() );
    	
    	JSONArray docs = new JSONArray();
	    
	    for( Integer user : hm.keySet() ) {
	    	JSONObject doc = new JSONObject();
	    	doc.put( "docid", user );
	    	
	    	JSONArray terms = new JSONArray();
	    	HashMap<Integer,Double> hm2 = hm.get( user );
	    	
	    	for( Integer movie : hm2.keySet() ) {
	    		terms.put( String.valueOf( movie ) );
	    	}
	    	
	    	doc.put( "terms", terms );
	    	docs.put( doc );
	    }
	    
    	json.put( "docs", docs );
    	out.println( json.toString() );
    	out.close();
    	//System.out.println( json.toString( 4 ) );
    }
}

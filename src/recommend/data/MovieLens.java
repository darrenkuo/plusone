package recommend.data;

import java.io.*;
import java.util.*;

import org.json.*;

public class MovieLens {
	public static void main( String[] args ) throws Throwable {
	    Scanner in = new Scanner( new File( "ratings.dat" ) );
    	PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( "movielens-pos.json" ) ) );
	    String s;
	    HashMap<Integer,HashSet<Integer>> hm = new HashMap<Integer,HashSet<Integer>>();
	    HashSet<Integer> movies = new HashSet<Integer>();
	    
	    while( in.hasNextLine() ) {
	    	String[] arr = in.nextLine().split( "::" );
	    	int user = Integer.parseInt( arr[0] );
	    	int movie = Integer.parseInt( arr[1] );
	    	int rating = Integer.parseInt( arr[2] );
	    	int time = Integer.parseInt( arr[3] );
	    	
	    	if( rating < 3 ) {
	    		continue;
	    	}
	    	
	    	if( hm.containsKey( user ) ) {
	    		hm.get( user ).add( movie );
	    	} else {
	    		hm.put( user, new HashSet<Integer>() );
	    		hm.get( user ).add( movie );
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
	    	
	    	for( Integer movie : hm.get( user ) ) {
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

package recommend.data;

import java.io.*;
import java.util.*;

import org.json.*;

public class MovieLens {
	public static void main( String[] args ) throws Throwable {
	    Scanner in = new Scanner( new File( "rawdata/ratings.dat" ) );
    	PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( "reg_movielens.json" ) ) );
	    String s;
	    HashMap<Integer,HashSet<Pair>> hm = new HashMap<Integer,HashSet<Pair>>();
	    
	    while( in.hasNextLine() ) {
	    	String[] arr = in.nextLine().split( "::" );
	    	int user = Integer.parseInt( arr[0] );
	    	int movie = Integer.parseInt( arr[1] );
	    	int rating = Integer.parseInt( arr[2] );
	    	int time = Integer.parseInt( arr[3] );
	    	
	    	if( hm.containsKey( user ) ) {
	    		hm.get( user ).add( new Pair( movie, rating ) );
	    	} else {
	    		hm.put( user, new HashSet<Pair>() );
	    		hm.get( user ).add( new Pair( movie, rating ) );
	    	}
	    }
	    
	    JSONObject json = new JSONObject();
    	json.put( "num_users", hm.keySet().size() );
    	JSONArray users = new JSONArray();
	    
	    for( Integer name : hm.keySet() ) {
	    	JSONObject user = new JSONObject();
	    	user.put( "name", String.valueOf( name ) );
	    	JSONArray items = new JSONArray();
	    	JSONArray scores = new JSONArray();
	    	
	    	for( Pair p : hm.get( name ) ) {
	    		items.put( String.valueOf( p.movie ) );
	    		scores.put( p.rating );
	    	}
	    	
	    	user.put( "items", items );
	    	user.put( "scores", scores );
	    	users.put( user );
	    }
	    
    	json.put( "users", users );
    	out.println( json.toString() );
    	out.close();
    	//System.out.println( json.toString( 4 ) );
    }
	
	static class Pair {
		int movie;
		int rating;
		
		public Pair( int movie, int rating ) {
			this.movie = movie;
			this.rating = rating;
		}
	}
}

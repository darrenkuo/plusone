package recommend.data;

import org.json.*;

import java.io.*;
import java.util.*;

public class Jester {
	public static void main( String[] args ) throws Throwable {
	    JSONObject json = new JSONObject();
	    json.put( "num_users", 24983+23500+24938 );
    	JSONArray users = new JSONArray();
	    
	    Scanner in = new Scanner( new File( "rawdata/jester-data.csv" ) );
    	PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( "reg_jester.json" ) ) );
	    
	    for( int i = 0; i < 24983+23500+24938; i++ ) {
	    	JSONObject user = new JSONObject();
	    	user.put( "name", String.valueOf( i ) );
	    	JSONArray items = new JSONArray();
	    	JSONArray scores = new JSONArray();
	    	in.nextInt();
	    	
	    	for( int j = 0; j < 100; j++ ) {
	    		double rating = in.nextDouble();
	    		
	    		if( rating != 99 ) {
	    			items.put( Integer.toString( j ) );
	    			scores.put( rating );
	    		}
	    	}
	    	
	    	user.put( "items", items );
	    	user.put( "scores", scores );
	    	users.put( user );
	    }
	    
    	json.put( "users", users );
    	out.println( json.toString() );
    	out.close();
    }
}

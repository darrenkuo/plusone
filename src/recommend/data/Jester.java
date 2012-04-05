package recommend.data;

import org.json.*;

import java.io.*;
import java.util.*;

public class Jester {
	public static void main( String[] args ) throws Throwable {
	    JSONObject json = new JSONObject();
	    json.put( "ndocs", 24983+23500+24938 );
	    json.put( "nterms", 100 );
    	JSONArray docs = new JSONArray();
	    
	    Scanner in = new Scanner( new File( "jester-data.csv" ) );
    	PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( "jester-pos.json" ) ) );
	    
	    for( int i = 0; i < 24983+23500+24938; i++ ) {
	    	JSONObject doc = new JSONObject();
	    	doc.put( "docid", i );
	    	JSONArray terms = new JSONArray();
	    	in.nextInt();
	    	
	    	for( int j = 0; j < 100; j++ ) {
	    		double rating = in.nextDouble();
	    		
	    		if( rating != 99 && rating > 5 ) {
	    			terms.put( Integer.toString( j ) );
	    		}
	    	}
	    	
	    	doc.put( "terms", terms );
	    	docs.put( doc );
	    }
	    
    	json.put( "docs", docs );
    	out.println( json.toString() );
    	out.close();
    }
}

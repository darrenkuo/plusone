package recommend.data;

import java.io.*;
import java.util.*;
import org.json.*;

public class StackOverflow {
	static final String APIKEY = "2RbWLXSn7UmOZsu5FXT65Q";
	static final int SIZE = 1000;
	
	public static void main( String[] args ) throws Throwable {
	    Connection conn = new Connection( "http://api.stackoverflow.com" );
	    JSONArray questions = new JSONArray();
	    int page = 1;
	    
	    while( questions.length() < SIZE ) {
	    	String s = conn.get( "/1.1/questions?key=" + APIKEY + "&answers=true&body=true&comments=false&fromdate=1293840000&min=1&pagesize=100&sort=votes&page="+page );
	    	JSONObject json = new JSONObject( s );
	    	JSONArray q = json.getJSONArray( "questions" );
	    	
	    	for( int i = 0; i < q.length(); i++ ) {
	    		questions.put( q.get( i ) );
	    	}
	    	
	    	page++;
	    }
	    
	    JSONObject json = new JSONObject();
	    json.put( "questions", questions );
	    
	    PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( "stackoverflow.1000.raw" ) ) );
	    out.println( json.toString() );
	    out.close();
    }
}

package recommend.data;

import java.io.*;
import java.util.*;

import org.json.*;

public class Original {
	public static void main( String[] args ) throws Throwable {
		BufferedReader in = new BufferedReader( new FileReader( "test.out" ) );
    	PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( "test.json" ) ) );
		StringTokenizer st;

	    JSONObject json = new JSONObject();
    	JSONArray docs = new JSONArray();
    	int ndocs = 0;
		
		while( in.readLine() != null ) {
			ndocs++;
			int id = Integer.parseInt( in.readLine().substring( 7 ) )-1;
			st = new StringTokenizer( in.readLine().substring( 7 ) );
			int[] inref = new int[st.countTokens()];
			
			for( int i = 0; i < inref.length; i++ )
				inref[i] = Integer.parseInt( st.nextToken() );
			
			st = new StringTokenizer( in.readLine().substring( 8 ) );
			int[] outref = new int[st.countTokens()];
			
			for( int i = 0; i < outref.length; i++ )
				outref[i] = Integer.parseInt( st.nextToken() );
			
			st = new StringTokenizer( in.readLine().substring( 9 ) );
			HashSet<String> words = new HashSet<String>();
			
			while( st.hasMoreTokens() ) {
				words.add( st.nextToken() );
			}
			
			JSONObject doc = new JSONObject();
			doc.put( "docid", id );
			
			JSONArray terms = new JSONArray();
	    	
	    	for( String word : words ) {
	    		terms.put( word );
	    	}
	    	
	    	doc.put( "terms", terms );
	    	docs.put( doc );
			in.readLine();
		}
	    
    	json.put( "ndocs", ndocs );
    	json.put( "nterms", WordIndex.size() );
    	json.put( "docs", docs );
    	out.println( json.toString() );
    	out.close();
    	//System.out.println( json.toString( 4 ) );
    }
}

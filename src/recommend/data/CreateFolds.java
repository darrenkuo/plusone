package recommend.data;

import java.io.*;
import java.util.*;

import org.json.*;

import recommend.util.WordIndex;

public class CreateFolds {
	static int NFOLDS = 10;
	static String INFILE = "movielens-all.json";
	static String OUTFILE = "movielens-all10.json";
	
	public static void main( String[] args ) throws Throwable {
	    BufferedReader in = new BufferedReader( new FileReader( INFILE ) );
	    JSONObject json = new JSONObject( in.readLine() );
	    
        int ndocs = json.getInt( "ndocs" );
        ArrayList<HashMap<Integer,Double>> docs = new ArrayList<HashMap<Integer,Double>>( ndocs );
        JSONArray arr = json.getJSONArray( "docs" );
        
        for( int i = 0; i < ndocs; i++ ) {
            JSONObject doc = arr.getJSONObject( i );
            HashMap<Integer,Double> hm = new HashMap<Integer,Double>();
            docs.add( i, hm );
            JSONArray terms = doc.getJSONArray( "terms" );

            for( int j = 0; j < terms.length(); j++ ) {
                String term = terms.getString( j );
                WordIndex.add( terms.getString( j ) );
                int index = WordIndex.indexOf( term );
                
                if( hm.containsKey( index ) ) {
                	hm.put( index, hm.get( index )+1.0 );
                } else {
                	hm.put( index, 1.0 );
                }
            }
        }
        
        PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( OUTFILE ) ) );
        json = new JSONObject();
        json.put( "ndocs", ndocs );
        json.put( "nterms", WordIndex.size() );
        json.put( "nfolds", NFOLDS );
        
        JSONArray termindex = new JSONArray();
        
        for( int i = 0; i < WordIndex.size(); i++ ) {
        	termindex.put( WordIndex.get( i ) );
        }
        
        json.put( "termindex", termindex );
        
        JSONArray docindex = new JSONArray();
        
        for( int i = 0; i < ndocs; i++ ) {
        	docindex.put( Integer.toString( i+1 ) );
        }
        
        json.put( "docindex", docindex );
        
        JSONArray folds = new JSONArray();
        Collections.shuffle( docs );
        int foldsize = ndocs/NFOLDS;
        int remainder = ndocs - NFOLDS*foldsize;
        int i = 0;
        
        for( int j = 0; j < NFOLDS; j++ ) {
        	JSONArray fold = new JSONArray();
        	int limit = j < remainder ? foldsize+1 : foldsize;
        	
        	for( int k = 0; k < limit; k++ ) {
        		JSONArray doc = new JSONArray();
        		HashMap<Integer,Double> hm = docs.get( i++ );
        		ArrayList<Integer> keys = new ArrayList<Integer>( hm.size() );
        		
        		for( int w : hm.keySet() ) {
        			keys.add( w );
        		}
        		
        		Collections.shuffle( keys );
        		
        		for( int w : keys ) {
            		doc.put( new JSONArray( "[" + w + "," + hm.get( w ) + "]" ) );
        		}
        		
        		fold.put( doc );
        	}
        	
        	folds.put( fold );
        }
        
        json.put( "folds", folds );
    	out.println( json.toString() );
    	out.close();
    	//System.out.println(json.toString( 1 ));
    }
}

package recommend.data;

import java.io.*;
import java.util.*;

import org.json.*;

import recommend.util.WordIndex;

public class CreateFolds {
	static int NFOLDS = 5;
	static String INFILE = "reg_jester.json";
	static String OUTFILE = "reg_jester5.json";
	
	public static void main( String[] args ) throws Throwable {
	    BufferedReader in = new BufferedReader( new FileReader( INFILE ) );
	    JSONObject json = new JSONObject( in.readLine() );
	    
        int num_users = json.getInt( "num_users" );
        ArrayList<JSONObject> users = new ArrayList<JSONObject>();
        ArrayList<String> usernames = new ArrayList<String>();
        JSONArray arr = json.getJSONArray( "users" );
        
        for( int i = 0; i < num_users; i++ ) {
            JSONObject user = arr.getJSONObject( i );
            String name = user.getString( "name" );
            user.remove( "name" );
            user.put( "id", usernames.size() );
            usernames.add( name );
            
            JSONArray items = user.getJSONArray( "items" );
            JSONArray scores = user.getJSONArray( "scores" );

            for( int j = 0; j < items.length(); j++ ) {
                String item = items.getString( j );
                WordIndex.add( items.getString( j ) );
                int index = WordIndex.indexOf( item );
                items.put( j, index );
            }
            
            for( int j = items.length()-1; j >= 0; j-- ) {
                int rand = (int)( (j+1)*Math.random() );
                int t1 = items.getInt( j );
                items.put( j, items.get( rand ) );
                items.put( rand, t1 );
                double t2 = scores.getDouble( j );
                scores.put( j, scores.get( rand ) );
                scores.put( rand, t2 );
            }
            
            users.add( user );
        }
        
        PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( OUTFILE ) ) );
        json = new JSONObject();
        json.put( "num_users", num_users );
        json.put( "num_items", WordIndex.size() );
        json.put( "num_folds", NFOLDS );
        
        JSONArray userindex = new JSONArray();
        
        for( int i = 0; i < num_users; i++ ) {
        	userindex.put( usernames.get( i ) );
        }
        
        json.put( "userindex", userindex );
        
        JSONArray itemindex = new JSONArray();
        
        for( int i = 0; i < WordIndex.size(); i++ ) {
        	itemindex.put( WordIndex.get( i ) );
        }
        
        json.put( "itemindex", itemindex );
        
        JSONArray folds = new JSONArray();
        Collections.shuffle( users );
        int foldsize = num_users/NFOLDS;
        int remainder = num_users - NFOLDS*foldsize;
        int i = 0;
        
        for( int j = 0; j < NFOLDS; j++ ) {
        	JSONArray fold = new JSONArray();
        	int limit = j < remainder ? foldsize+1 : foldsize;
        	
        	for( int k = 0; k < limit; k++ ) {
        		fold.put( users.get( i++ ) );
        	}
        	
        	folds.put( fold );
        }
        
        json.put( "folds", folds );
    	out.println( json.toString() );
    	out.close();
    	//System.out.println(json.toString( 1 ));
    }
}

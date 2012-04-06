package recommend.util;

import java.io.*;
import java.util.HashMap;

import org.json.*;

public class Dataset {
	public int num_users, num_items, num_folds;
	public String[] itemindex, userindex;
	public HashMap<Integer,Double>[] users;
	public HashMap<Integer,Double>[][] folds;
	
	public Dataset( String file ) throws IOException, JSONException {
		BufferedReader in = new BufferedReader( new FileReader( file ) );
		JSONObject json = new JSONObject( in.readLine() );
		
		this.num_users = json.getInt( "num_users" );
		this.num_items = json.getInt( "num_items" );
		this.num_folds = json.getInt( "num_folds" );
		
		JSONArray itemindex = json.getJSONArray( "itemindex" );
		this.itemindex = new String[num_items];
		
		for( int i = 0; i < num_items; i++ ) {
			this.itemindex[i] = itemindex.getString( i );
		}
		
		JSONArray userindex = json.getJSONArray( "userindex" );
		this.userindex = new String[num_users];
		
		for( int i = 0; i < num_users; i++ ) {
			this.userindex[i] = userindex.getString( i );
		}
		
		JSONArray folds = json.getJSONArray( "folds" );
		this.users = new HashMap[num_users];
		this.folds = new HashMap[num_folds][];
		int index = 0;
		
		for( int i = 0; i < num_folds; i++ ) {
			JSONArray fold = folds.getJSONArray( i );
			this.folds[i] = new HashMap[fold.length()];
			
			for( int j = 0; j < fold.length(); j++ ) {
				JSONObject user = fold.getJSONObject( j );
				this.folds[i][j] = new HashMap<Integer,Double>();
				JSONArray items = user.getJSONArray( "items" );
				JSONArray scores = user.getJSONArray( "scores" );
				
				for( int k = 0; k < items.length(); k++ ) {
					this.folds[i][j].put( items.getInt( k ), scores.getDouble( k ) );
				}
				
				users[index++] = this.folds[i][j];
			}
		}
	}
}

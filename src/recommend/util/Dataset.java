package recommend.util;

import java.io.*;
import java.util.HashMap;

import org.json.*;

public class Dataset {
	public int ndocs, nterms, nfolds;
	public String[] termindex, docindex;
	public HashMap<Integer,Double>[] docs;
	public HashMap<Integer,Double>[][] folds;
	
	public Dataset( String file ) throws IOException, JSONException {
		BufferedReader in = new BufferedReader( new FileReader( file ) );
		JSONObject json = new JSONObject( in.readLine() );
		
		this.ndocs = json.getInt( "ndocs" );
		this.nterms = json.getInt( "nterms" );
		this.nfolds = json.getInt( "nfolds" );
		
		JSONArray termindex = json.getJSONArray( "termindex" );
		this.termindex = new String[nterms];
		
		for( int i = 0; i < nterms; i++ ) {
			this.termindex[i] = termindex.getString( i );
		}
		
		JSONArray docindex = json.getJSONArray( "docindex" );
		this.docindex = new String[ndocs];
		
		for( int i = 0; i < ndocs; i++ ) {
			this.docindex[i] = docindex.getString( i );
		}
		
		JSONArray folds = json.getJSONArray( "folds" );
		this.docs = new HashMap[ndocs];
		this.folds = new HashMap[nfolds][];
		int index = 0;
		
		for( int i = 0; i < nfolds; i++ ) {
			JSONArray fold = folds.getJSONArray( i );
			this.folds[i] = new HashMap[fold.length()];
			
			for( int j = 0; j < fold.length(); j++ ) {
				JSONArray doc = fold.getJSONArray( j );
				this.folds[i][j] = new HashMap<Integer,Double>();
				
				for( int k = 0; k < doc.length(); k++ ) {
					JSONArray term = doc.getJSONArray( k );
					this.folds[i][j].put( term.getInt( 0 ), term.getDouble( 1 ) );
				}
				
				docs[index++] = this.folds[i][j];
			}
		}
	}
}

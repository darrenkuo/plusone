package plusone.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;
import org.json.*;

public class DatasetJSON {

    class Paper {
		Map<Integer,Integer> abstractWords;
		Integer index;
		Integer group;
	
		public Paper(Integer index, Map<Integer,Integer> abstractWords) {
		    this.abstractWords = abstractWords;
		    this.index = index;
		}
    }

    /* Member fields. */
	public int num_users, num_items, num_folds;
	public String[] itemindex, userindex;
	public HashMap<Integer,Integer>[] users;
	public HashMap<Integer,Integer>[][] folds;
	
    List<PaperAbstract> documents = new ArrayList<PaperAbstract>();
    public List<PaperAbstract> getDocuments() { return documents; }

    Indexer<String> wordIndexer = new Indexer<String>();
    public Indexer<String> getWordIndexer() { return wordIndexer; }

    private Indexer<PaperAbstract> paperIndexer = 
	new Indexer<PaperAbstract>();
    public Indexer<PaperAbstract> getPaperIndexer() { return paperIndexer; }

    /* Private method used by loadDataset. */
    void loadInPlaceFromPath(String filename) {	
		try {
			BufferedReader in = new BufferedReader( new FileReader( filename ) );
			JSONObject json = new JSONObject( in.readLine() );
			
			this.num_users = json.getInt( "num_users" );
			this.num_items = json.getInt( "num_items" );
			
			JSONArray users = json.getJSONArray( "users" );
			
			for( int i = 0; i < num_items; i++ ) {
				this.wordIndexer.fastAddAndGetIndex(i + "");
			}
			
			int index = 0;
			JSONArray items = null, scores = null;
			
			for( int i = 0; i < num_users; i++ ) {
				JSONObject user = users.getJSONObject( i );
				HashMap<Integer, Integer> tf = new HashMap<Integer, Integer>();
				items = user.getJSONArray( "items" );
				try	{
					//If successful, this is a regression file
					scores = user.getJSONArray( "scores" );
				} catch (JSONException e) {
					scores = null;
				}
				for( int j = 0; j < items.length(); j++ ) {
					String jthItem = items.getString( j );
					if (scores == null) {
						if ( !tf.keySet().contains(this.wordIndexer.fastIndexOf(jthItem))) {
							tf.put( this.wordIndexer.fastAddAndGetIndex(jthItem), 1 );
						} else {
							tf.put( this.wordIndexer.fastIndexOf(jthItem), tf.get( this.wordIndexer.fastIndexOf(jthItem) ) + 1 );
						}
					} else {
						tf.put( this.wordIndexer.fastAddAndGetIndex(jthItem), scores.getInt( j ) );
					}
				}
				PaperAbstract p = new PaperAbstract(index++, null, null, tf);
				documents.add(p);
				paperIndexer.add(p);
			}
		} catch(Exception e) {
		    e.printStackTrace();
		}

		System.out.println("total number of papers: " + documents.size());
    }

    /** This method is to be called in order to construct a datasetJSON
     * 
     * @param filename The path to the JSON file being loaded
     * @return a DatasetJSON with its document, wordIndexer, and paperIndexer fields instantiated with the information contained in the JSON
     */
    public static DatasetJSON loadDatasetFromPath(String filename) {
        DatasetJSON dataset = new DatasetJSON();
        dataset.loadInPlaceFromPath(filename);
        return dataset;
    }
}

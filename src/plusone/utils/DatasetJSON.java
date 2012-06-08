package plusone.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	public String[] itemindex, userindex;
	public HashMap<Integer,Integer>[] users;
	
    List<PaperAbstract> documents = new ArrayList<PaperAbstract>();
    public List<PaperAbstract> getDocuments() { return documents; }

    Indexer<String> wordIndexer = new Indexer<String>();
    public Indexer<String> getWordIndexer() { return wordIndexer; }

    private Indexer<PaperAbstract> paperIndexer = 
	new Indexer<PaperAbstract>();
    public Indexer<PaperAbstract> getPaperIndexer() { return paperIndexer; }

    /** Reads in the JSON file and fills in documents, wordIndexer, and
     * paperIndexer appropriately. Will automatically change its behavior when
     * presented with a regression task.
     * 
     * @param filename path to the JSON file (should be passed from loadDatasetFromPath)
     */
    void loadInPlaceFromPath(String filename) {	
		try {
			BufferedReader in = new BufferedReader( new FileReader( filename ) );
			JSONObject json = new JSONObject( in.readLine() );
						
			JSONArray users = json.getJSONArray( "users" );
			if (isIndexed(filename)) {
				initializeIndexer(filename);
			}
			
			int index = 0;
			JSONArray items = null, scores = null;
			for( int i = 0; i < users.length(); i++ ) {
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
    
    
    /**
     * Returns true if the input is already indexed
     * 
     * @param filename filename path to the JSON file (should be passed from loadDatasetFromPath)
     * @return
     */
    private boolean isIndexed(String filename) {
    	try {
			BufferedReader in = new BufferedReader( new FileReader( filename ) );
	    	JSONObject json = new JSONObject( in.readLine() );
			JSONArray users = json.getJSONArray( "users" );
			for (int i = 0; i < users.length(); i++) {
				JSONObject user = users.getJSONObject( i );
				JSONArray items = user.getJSONArray( "items" );
				for (int j = 0; j < items.length(); j++) {
					try {
						int unused = new Integer(items.getString(j));
					} catch (NumberFormatException e) {
						return false;
					}
				}
			}
			return true;
    	} catch (Exception e) {
    		System.out.println("Something went wrong with isIndexed");
    		return false;
    	}
    }
    
    /**
     * If we have pre-indexed files, initialize the indexer with those indices
     * @param filename filename path to the JSON file (should be passed from loadDatasetFromPath)
     */
    private void initializeIndexer(String filename) {
    	int maxIndex = -1;
    	try {
			BufferedReader in = new BufferedReader( new FileReader( filename ) );
	    	JSONObject json = new JSONObject( in.readLine() );
			JSONArray users = json.getJSONArray( "users" );
			for (int i = 0; i < users.length(); i++) {
				JSONObject user = users.getJSONObject( i );
				JSONArray items = user.getJSONArray( "items" );
				for (int j = 0; j < items.length(); j++) {
					if (Integer.parseInt(items.getString(j)) > maxIndex) {
						maxIndex = Integer.parseInt(items.getString(j));
					}
				}

			}
    	} catch (Exception e) {
    		System.out.println("Couldn't initialize indexer for pre-indexed files");
    	}
    	System.out.println("Max index is " + maxIndex);
    	for (int i = 0; i < maxIndex; i++) {
    		this.wordIndexer.fastAddAndGetIndex(i  +"");
    	}
    }
}

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
			this.num_folds = json.getInt( "num_folds" );
			
			JSONArray itemindex = json.getJSONArray( "itemindex" );
			
			for( int i = 0; i < num_items; i++ ) {
				this.wordIndexer.fastAddAndGetIndex(itemindex.getString(i));
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
			JSONArray items = null, scores;
			
			for( int i = 0; i < num_folds; i++ ) {
				JSONArray fold = folds.getJSONArray( i );
				this.folds[i] = new HashMap[fold.length()];
				
				for( int j = 0; j < fold.length(); j++ ) {
					JSONObject user = fold.getJSONObject( j );
					this.folds[i][j] = new HashMap<Integer,Integer>();
					items = user.getJSONArray( "items" );
					scores = user.getJSONArray( "scores" );
					//put the jth user of the ith fold in folds[i][j]
					for( int k = 0; k < items.length(); k++ ) {
						this.folds[i][j].put( items.getInt( k ), scores.getInt( k ) );
					}
					
					PaperAbstract p = new PaperAbstract(index++, null, null, this.folds[i][j]);
					documents.add(p);
					paperIndexer.add(p);
				}
			}
	
			for (int i = 0; i < num_items; i ++) {
				wordIndexer.fastAddAndGetIndex(itemindex.getString(i));
			}
		} catch(Exception e) {
		    e.printStackTrace();
		}

		System.out.println("total number of papers: " + documents.size());
    }

    public static DatasetJSON loadDatasetFromPath(String filename) {
        DatasetJSON dataset = new DatasetJSON();
        dataset.loadInPlaceFromPath(filename);
        return dataset;
    }
}

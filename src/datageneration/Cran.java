package datageneration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Cran {

	/**
	 * Makes a json from the cran dataset at
	 * http://www.dataminingresearch.com/index.php/2010/09/classic3-classic4-datasets/
	 * Replace all instances of "cran" in the source with "cacm", "cisi", or "med" 
	 * and change the number of iterations in the first for loop to run
	 * this program on another dataset
	 * 
	 * @param args unused
	 * @throws JSONException 
	 */
	public static void main(String[] args) throws Throwable {
		File input;
    	PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( "cacm.json" ) ) );
    	ArrayList<String> stopWords = makeStopWords();
		HashMap<Integer, ArrayList<String>> hm = new HashMap<Integer,ArrayList<String>>();
		for (int i = 1; i <= 3204; i++) {
			ArrayList<String> thisDoc = new ArrayList<String>();
			if (i < 10)
				input = new File("data/classic/cacm.00000" + i);
			else if (i < 100)
				input = new File("data/classic/cacm.0000" + i);
			else if (i < 1000)
				input = new File("data/classic/cacm.000" + i);
			else
				input = new File("data/classic/cacm.00" + i);
			Scanner in = null;
			try {
				in = new Scanner(input);
				while (in.hasNext()) {
					String word = in.next();
					if (!stopWords.contains(word.toLowerCase()))
						thisDoc.add(word);
				}
				hm.put(i, thisDoc);
			} catch (FileNotFoundException e) {
				System.out.println("Could not find: " + i);
			}
		}

		JSONObject json = new JSONObject();
		
		JSONArray docs = new JSONArray();

	    for( Integer user : hm.keySet() ) {
	    	JSONObject doc = new JSONObject();
	    	doc.put( "id", user );

	    	JSONArray terms = new JSONArray();
	    	ArrayList<String> thisUser = hm.get(user);

	    	for( String word : thisUser ) {
	    		terms.put(word);
	    	}

	    	doc.put( "items", terms );
	    	//doc.put( "scores", scores );
	    	docs.put( doc );
	    }
    	json.put( "users", docs );
    	out.println( json.toString() );
    	out.close();
	}
	
	private static ArrayList<String> makeStopWords() {
		ArrayList<String> result = new ArrayList<String>();
		String[] words = {"a", "an", ".", "the", "by", "to", "was", "or", "in", "of",
				"for", "with", "and", "on", "is", "at", "as", "it"};
		for (String s : words) {
			result.add(s);
		}
		return result;
	}

}

package datageneration;

import java.io.*;
import java.util.*;

import org.json.*;

/** Makes a JSON file corresponding to a file given by args[0]. If args[1] is "true",
 *  the input will have the (integer) indices of items on each odd numbered line, with each item's score on the next even
 *  numbered line. If args[1] is "false", the each line of the input will correspond to a different document and each
 *  number will be the items in the document. Consider the input:
 *  1 3 4
 *  2 2 5
 *  1 7 9 20
 *  1 3 4 2
 *  If regression = true, this is parsed as having 2 documents, the first having three items {1, 3, 4} with respective scores {2, 2, 5} and the
 *  second having 4 items {1, 7, 9, 20} with respective scores {1, 3, 4, 2}. If regression = false, this is parsed as having 4 documents, the first having
 *  items {1, 3, 4}, the second having items {2 (twice), 5}, etc.
 *  
 *  The JSON file is printed on standard out.
 */
public class MakeJSON {
	public static void main(String[] args) throws IOException, JSONException {
    	PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( "out2.json" ) ) );
		
    	//Location of the file
		//String thisfile = args[0];
    	String thisfile = "/Users/andrewgambardella/Downloads/documents-out";
		//Set to true if each item has a corresponding score, false otherwise
		boolean regression = false;
		/*if (args[1].equals("true")) {
			regression = true;
		}*/
		File fixedFile = null;
		try {
			fixedFile = preprocess(thisfile.toString());
		} catch (IOException e) {
			System.out.println("Could not preprocess");
			System.exit(1);
		}
		
		Scanner lines = null;
		try {
			lines = new Scanner(new FileInputStream(fixedFile));
		} catch (FileNotFoundException e) {
			System.out.println("Could not get file from preprocess");
			System.exit(1);
		}
		
		if (regression) {
		    HashMap<Integer,ArrayList<Pair>> hm = new HashMap<Integer,ArrayList<Pair>>();
			int users = 0;
			int itemCount = 0;
			ArrayList<String> seenItems = new ArrayList<String>();
			while (lines.hasNextLine()) {
				String itemLine = lines.nextLine();
				String scoreLine = lines.nextLine();
				String[] items = itemLine.split(" ");
				String[] scores = scoreLine.split(" ");
				ArrayList<Pair> pairList = new ArrayList<Pair>();
				for (int i = 0; i < items.length; i++) {
					pairList.add(new Pair(Integer.parseInt(items[i]), Integer.parseInt(scores[i])));
					if (!seenItems.contains(items[i])) {
						seenItems.add(items[i]);
						itemCount++;
					}
				}
				hm.put(users, pairList);
				users++;
			}
			JSONObject json = new JSONObject();
			json.put( "num_users", hm.keySet().size() );
			json.put( "num_items", itemCount );
			
	    	JSONObject[] userArray = new JSONObject[hm.keySet().size()];
		    for( Integer name : hm.keySet() ) {
		    	JSONObject user = new JSONObject();
		    	user.put( "id",  name );
		    	JSONArray itemsJSON = new JSONArray();
		    	JSONArray scoresJSON = new JSONArray();
		    	
		    	for( Pair a : hm.get( name ) ) {
		    		itemsJSON.put( a.movie );
		    		scoresJSON.put( a.rating );
		    	}
		    	
		    	user.put( "items", itemsJSON );
		    	user.put( "scores", scoresJSON );
		    	userArray[name] = user;
		    }

		    json.put("users", userArray);
	    	out.println( json.toString() );
	    	out.close();
		} else {
			HashMap<Integer,ArrayList<Integer>> hm = new HashMap<Integer,ArrayList<Integer>>();
			int users = 0;
			int itemCount = 0;
			ArrayList<String> seenItems = new ArrayList<String>();
			while (lines.hasNextLine()) {
				String thisLine = lines.nextLine();
				String[] items = thisLine.split(" ");
				ArrayList<Integer> itemList = new ArrayList<Integer>();
				for (String s : items) {
					itemList.add(Integer.parseInt(s));
					if (!seenItems.contains(s)) {
						seenItems.add(s);
						itemCount++;
					}
				}
				hm.put(users, itemList);
				users++;
			}
			JSONObject json = new JSONObject();
			json.put( "num_users", hm.keySet().size() );
			json.put( "num_items", itemCount );
			
	    	JSONObject[] userArray = new JSONObject[hm.keySet().size()];
		    for( Integer name : hm.keySet() ) {
		    	JSONObject user = new JSONObject();
		    	user.put( "id",  name );
		    	JSONArray itemsJSON = new JSONArray();
		    	
		    	for( Integer a : hm.get( name ) ) {
		    		itemsJSON.put( a );
		    	}
		    	
		    	user.put( "items", itemsJSON );
		    	userArray[name] = user;
		    }

		    json.put("users", userArray);
	    	out.println( json.toString() );
	    	out.close();
		}
	}
	
	private static File preprocess(String filepath) throws IOException {
		File f = new File("temporaryfilepleaseignore.txt");
		f.deleteOnExit();
		FileWriter writer = null;
		try {
			writer = new FileWriter(f);
		} catch (IOException e) {
			System.out.println("Check your filepath");
			System.exit(1);
		}
		
		FileInputStream filecontents = null;
		try {
			filecontents = new FileInputStream(filepath);
		} catch (FileNotFoundException e) {
			System.out.println("Check your filepath");
			System.exit(1);
		}
		Scanner lines = new Scanner(filecontents);
		while (lines.hasNextLine()) {
			String itemLine = lines.nextLine();
			String scoreLine = lines.nextLine();
			ArrayList<Integer> removeTheseItems = new ArrayList<Integer>();
			String[] potentialItems = itemLine.split(" ");
			String[] potentialScores = scoreLine.split(" ");
			for (int i = 0; i < potentialScores.length; i++) {
				if (potentialScores[i].equals("0")) {
					removeTheseItems.add(i);
				}
			}

			for (int i = 0; i < potentialItems.length; i++) {
				if (!removeTheseItems.contains(i)) {
					writer.write(potentialItems[i] + " ");
				}
			}
			writer.write("\n");
			for (int i = 0; i < potentialScores.length; i++) {
				if (!removeTheseItems.contains(i)) {
					writer.write(potentialScores[i] + " ");
				}
			}
			writer.write("\n");
		}
		writer.close();
		
		return f;
	}
	
	static class Pair {
		int movie;
		int rating;
		
		public Pair( int movie, int rating ) {
			this.movie = movie;
			this.rating = rating;
		}
	}

}
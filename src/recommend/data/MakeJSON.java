package recommend.data;

import java.io.*;
import java.util.*;

/** Makes a JSON file corresponding to a file given by the string on the first line of the main method. If regression is true,
 *  the input will have the (integer) indices of items on each odd numbered line, with each item's score on the next even
 *  numbered line. If regression is false, the each line of the input will correspond to a different document and each
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
	public static void main(String[] args) throws IOException {
    	//PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( "out2.json" ) ) );
		
		//Needs to be changed depending on where file is located
		String thisfile = args[0];
		//Set to true if each item has a corresponding score, false otherwise
		boolean regression = false;
		if (args[1].equals("true")) {
			regression = true;
		}
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
		
		int count = 0;
		int numUsers = 0;
		int numItems = 0;
		System.out.print("{\"folds\":[");
		if (regression) {
			while (lines.hasNextLine()) {
				String thisLine = lines.nextLine();
				if ((count % 2) == 0) {
					String[] items = thisLine.split(" ");
					System.out.print("[{\"id\":" + ((count/2) + 1) + ",\"items\":[");
					for (int i = 0; i < items.length; i++) {
						System.out.print(items[i]);
						if (Integer.parseInt(items[i]) + 1 > numItems) {
							numItems = Integer.parseInt(items[i]) + 1;
						}
						if (i != items.length - 1) {
							System.out.print(",");
						}
					}
					System.out.print("],");
					count++;
					numUsers++;
				} else {
					String[] scores = thisLine.split(" ");
					System.out.print("\"scores\":[");
					for (int i = 0; i < scores.length; i++) {
						System.out.print(scores[i]);
						if (i != scores.length - 1) {
							System.out.print(",");
						}
					}
					if (lines.hasNextLine()) {
						System.out.print("]}],");
					} else {
						System.out.print("]}]],");
					}
					count++;
				}
			}
		} else {
			while (lines.hasNextLine()) {
				String thisLine = lines.nextLine();
				String[] items = thisLine.split(" ");
				System.out.print("[{\"id\":" + ((count) + 1) + ",\"items\":[");
				for (int i = 0; i < items.length; i++) {
					System.out.print(items[i]);
					if (Integer.parseInt(items[i]) + 1 > numItems) {
						numItems = Integer.parseInt(items[i]) + 1;
					}
					if (i != items.length - 1) {
						System.out.print(",");
					}
				}
				if (lines.hasNextLine()) {
					System.out.print("]}],");
				} else {
					System.out.print("]}]],");
				}
				count++;
				numUsers++;
			}
		}
		//items and scores printed
		System.out.print("\"itemindex\":[");
		for (int i = 1; i <= numItems; i++) {
			System.out.print("\"" + i + "\"");
			if (i != numItems) {
				System.out.print(",");
			}
		}
		System.out.print("],");
		//itemindex printed
		
		System.out.print("\"num_folds\":5,");
		System.out.print("\"num_items\":" + numItems + ",");
		System.out.print("\"num_users\":" + numUsers + ",");
		
		System.out.print("\"userindex\":[");
		for (int i = 1; i <= numUsers; i++) {
			System.out.print("\"" + i + "\"");
			if (i != numUsers) {
				System.out.print(",");
			}
		}
		System.out.print("]}");
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

}
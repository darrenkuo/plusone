package recommend.data;

import java.io.*;
import java.util.*;

public class MakeJSON {
	public static void main(String[] args) throws IOException {
    	PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( "out.json" ) ) );
		
		//Needs to be changed depending on where file is located
		StringBuffer filestem = new StringBuffer("/Users/andrewgambardella/Research/");
		StringBuffer thisfile = filestem.append("documents-out");
		boolean regression = false;
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
		out.print("{\"folds\":[");
		if (regression) {
			while (lines.hasNextLine()) {
				String thisLine = lines.nextLine();
				if ((count % 2) == 0) {
					String[] items = thisLine.split(" ");
					out.print("[{\"id\":" + ((count/2) + 1) + ",\"items\":[");
					for (int i = 0; i < items.length; i++) {
						out.print(items[i]);
						if (Integer.parseInt(items[i]) + 1 > numItems) {
							numItems = Integer.parseInt(items[i]) + 1;
						}
						if (i != items.length - 1) {
							out.print(",");
						}
					}
					out.print("],");
					count++;
					numUsers++;
				} else {
					String[] scores = thisLine.split(" ");
					out.print("\"scores\":[");
					for (int i = 0; i < scores.length; i++) {
						out.print(scores[i]);
						if (i != scores.length - 1) {
							out.print(",");
						}
					}
					if (lines.hasNextLine()) {
						out.print("]}],");
					} else {
						out.print("]}]],");
					}
					count++;
				}
			}
		} else {
			while (lines.hasNextLine()) {
				String thisLine = lines.nextLine();
				String[] items = thisLine.split(" ");
				out.print("[{\"id\":" + ((count) + 1) + ",\"items\":[");
				for (int i = 0; i < items.length; i++) {
					out.print(items[i]);
					if (Integer.parseInt(items[i]) + 1 > numItems) {
						numItems = Integer.parseInt(items[i]) + 1;
					}
					if (i != items.length - 1) {
						out.print(",");
					}
				}
				out.print("],");
				count++;
				numUsers++;
			}
		}
		//items and scores printed
		out.print("\"itemindex\":[");
		for (int i = 1; i <= numItems; i++) {
			out.print("\"" + i + "\"");
			if (i != numItems) {
				out.print(",");
			}
		}
		out.print("],");
		//itemindex printed
		
		out.print("\"num_folds\":5,");
		out.print("\"num_items\":" + numItems + ",");
		out.print("\"num_users\":" + numUsers + ",");
		
		out.print("\"userindex\":[");
		for (int i = 1; i <= numUsers; i++) {
			out.print("\"" + i + "\"");
			if (i != numUsers) {
				out.print(",");
			}
		}
		out.print("]}");
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

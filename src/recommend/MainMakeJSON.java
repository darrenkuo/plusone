package recommend;

import java.io.*;
import java.util.*;

public class MainMakeJSON {
	public static void main(String[] args) {
		
		//Needs to be changed depending on where file is located
		StringBuffer filestem = new StringBuffer("/Users/andrewgambardella/Research/");
		StringBuffer thisfile = filestem.append("test.txt");
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

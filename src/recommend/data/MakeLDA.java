package recommend.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class MakeLDA {

	public static void main(String[] args) throws IOException {
		//PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( "LDA.txt" ) ) );
		
		//Needs to be changed depending on where file is located
		StringBuffer filestem = new StringBuffer("/Users/andrewgambardella/Downloads/");
		StringBuffer thisfile = filestem.append("documents-out");
		boolean regression = false;
		
		FileInputStream filecontents = null;
		try {
			filecontents = new FileInputStream(thisfile.toString());
		} catch (FileNotFoundException e) {
			System.out.println("Check your filepath");
			System.exit(1);
		}
		Scanner lines = new Scanner(filecontents);
		
		while (lines.hasNextLine()) {
			if (regression) {
				String itemLine = lines.nextLine();
				String scoreLine = lines.nextLine();
				String[] items = itemLine.split(" ");
				String[] scores = scoreLine.split(" ");
				System.out.print(items.length + " ");
				for (int i = 0; i < items.length; i++) {
					System.out.print(items[i] + ":" + scores[i] + " ");
				}
				System.out.println();
			} else {
				StringBuffer toBePrinted = new StringBuffer();
				String itemLine = lines.nextLine();
				String[] items = itemLine.split(" ");
				int distinctItems = items.length;
				ArrayList<Integer> seen = new ArrayList<Integer>();
				for (int i = 0; i < items.length; i++) {
					Integer thisItem = Integer.parseInt(items[i]);
					if (!seen.contains(thisItem)) {
						seen.add(thisItem);
						int itemCount = 0;
						for (int j = 0; j < items.length; j++) {
							if (thisItem.equals(Integer.parseInt(items[j]))) {
								itemCount += 1;
							}
						}
						toBePrinted.append(items[i] + ":" + itemCount + " ");
					} else {
						distinctItems -= 1;
					}
				}
				System.out.println(distinctItems + " " + toBePrinted.toString());
			}
		}
	}

}

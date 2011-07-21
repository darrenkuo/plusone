package plusone;

import plusone.utils.PaperAbstract;

import plusone.clustering.Lda;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    List<PaperAbstract> load_data(String filename) {
	List<PaperAbstract> results = new ArrayList<PaperAbstract>();

	String index_pattern_string = "#INDEX ([\\d]+)";
	Pattern index_pattern = Pattern.compile(index_pattern_string);

	String inref_pattern_string = "#IN-REF ([\\d\\s]+)";
	Pattern inref_pattern = Pattern.compile(inref_pattern_string);

	String outref_pattern_string = "#OUT-REF ([\\d\\s]+)";
	Pattern outref_pattern = Pattern.compile(outref_pattern_string);

	String abstract_pattern_string = "#ABSTRACT ([\\s\\S]+)";
	Pattern abstract_pattern = Pattern.compile(abstract_pattern_string);

	try {
	    FileInputStream fstream = new FileInputStream(filename);
	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = 
		new BufferedReader(new InputStreamReader(in));
	    String strLine;
	    
	    while ((strLine = br.readLine()) != null) {
		System.out.println("started the loop with: " + strLine);
		if (!strLine.equals( "<!--")) {
		    System.out.println("Bad start...error soon!");
		    System.out.println("found " + strLine + "instead.");
		}

		int index = -1;
		int[] inRef = null;
		int[] outRef = null;
		String abstractText = null;
		
		strLine = br.readLine();
		Matcher matcher = index_pattern.matcher(strLine);
		if (matcher.matches()) {
		    index = new Integer(matcher.group(1));
		}

		strLine = br.readLine();
		matcher = inref_pattern.matcher(strLine);
		if (matcher.matches()) {
		    String matched_string = matcher.group(1);
		    String[] array = matched_string.split(" ");
		    inRef = new int[array.length];
		    for (int i = 0; i < array.length; i ++) {
			inRef[i] = new Integer(array[i]);
		    }
		}

		strLine = br.readLine();
		matcher = outref_pattern.matcher(strLine);
		if (matcher.matches()) {
		    String matched_string = matcher.group(1);
		    String[] array = matched_string.split(" ");
		    outRef = new int[array.length];
		    for (int i = 0; i < array.length; i ++) {
			outRef[i] = new Integer(array[i]);
		    }
		}

		strLine = br.readLine();
		matcher = abstract_pattern.matcher(strLine);
		if (matcher.matches()) {
		    abstractText = matcher.group(1);
		}

		System.out.println("parse results:");
		System.out.println("index: " + index);
		System.out.println("in ref: " + Arrays.toString(inRef));
		System.out.println("out ref: " + Arrays.toString(outRef));
		System.out.println("abstract: " + abstractText);
				   
		strLine = br.readLine();

		results.add(new PaperAbstract(index, inRef, 
					      outRef, abstractText));
	    }
	    br.close();
	} catch(Exception e) {
	    e.printStackTrace();
	}
	    
	return results;
    }

    /*
     * data - args[1]
     * train percent - args[2]
     * test word percent - args[3]
     * 
     */
    public static void main(String[] args) {
	if (args.length < 3) {
	    System.out.println("Please specify correct arguments:");
	    System.out.println("java -cp Plusone.jar Main <data file name> <float percent of the data for training> <float percent of the words for testing>");
	    System.exit(0);
	}
	
	String data_file = args[0];

	if (!new File(data_file).exists()) {
	    System.out.println("Data file does not exist.");
	    System.exit(0);
	}

	float train_percent = new Float(args[1]);
	float test_word_percent = new Float(args[2]);
	
	System.out.println("data file " + data_file);
	System.out.println("train percent " + train_percent);
	System.out.println("test word percent " + test_word_percent);

	Main main = new Main();

        List<PaperAbstract> documents = main.load_data(data_file);
	System.out.println("document set size: " + documents.size());

	List<PaperAbstract> trainingSet = 
	    documents.subList(0, ((int)(documents.size() * train_percent)));
	List<PaperAbstract> testingSet = 
	    documents.subList((int)(documents.size() * train_percent) + 1, 
			      documents.size());
	documents = null;

	System.out.println("training set size: " + trainingSet.size());
	System.out.println("testing set size: " + testingSet.size());

	new Lda().train(trainingSet);
    }
}
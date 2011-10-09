package plusone;

import plusone.utils.Indexer;
import plusone.utils.PaperAbstract;
import plusone.utils.PlusoneFileWriter;
import plusone.utils.Term;

import plusone.clustering.Baseline;
import plusone.clustering.Baseline1;
import plusone.clustering.ClusteringTest;
import plusone.clustering.KNN;
import plusone.clustering.KNNWithCitation;
import plusone.clustering.KNNWithCitationBF;
import plusone.clustering.Lda;
import plusone.clustering.LSI;

import java.io.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private Indexer<String> wordIndexer;
    private Indexer<PaperAbstract> paperIndexer;
    private Map<Integer, Integer> paperIndexMap;

    // Document sets
    public List<PaperAbstract> documents;
    public List<PaperAbstract> trainingSet;
    public List<PaperAbstract> testingSet;

    // Clustering Methods
    private Baseline base;
    private Baseline1 base1;
    private KNN knn;
    private LSI lsi;

    public List<PaperAbstract> load_data(String filename) {
	this.documents = new ArrayList<PaperAbstract>();

	String index_pattern_string = "#INDEX ([\\d]+)";
	Pattern index_pattern = Pattern.compile(index_pattern_string);

	String inref_pattern_string = "#IN-REF ([\\d\\s]+)";
	Pattern inref_pattern = Pattern.compile(inref_pattern_string);

	String outref_pattern_string = "#OUT-REF ([\\d\\s]+)";
	Pattern outref_pattern = Pattern.compile(outref_pattern_string);

	String abstract_pattern_string = "#ABSTRACT ([\\s\\S]+)";
	Pattern abstract_pattern = Pattern.compile(abstract_pattern_string);

	this.paperIndexer = new Indexer<PaperAbstract>();
	this.wordIndexer = new Indexer<String>();
	this.paperIndexMap = new HashMap<Integer, Integer>();

	try {
	    FileInputStream fstream = new FileInputStream(filename);
	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = 
		new BufferedReader(new InputStreamReader(in));
	    String strLine;
	    
	    while ((strLine = br.readLine()) != null) {
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

		strLine = br.readLine();

		PaperAbstract a = 
		    new PaperAbstract(index, inRef, outRef, abstractText, 
				      wordIndexer);
		documents.add(a);

		paperIndexMap.put(index, paperIndexer.addAndGetIndex(a));
	    }
	    br.close();
	} catch(Exception e) {
	    e.printStackTrace();
	}	    

	int inref_zero = 0;
	for (PaperAbstract a : documents) {
	    int i = 0;
	    ArrayList<Integer> references = new ArrayList<Integer>();
	    for (i = 0; i < a.inReferences.length; i ++) {
		Integer j = paperIndexMap.get(a.inReferences[i]);
		if (j != null) {
		    references.add(j);
		}
	    }

	    a.inReferences = new int[references.size()];
	    i = 0;
	    for (Integer j : references) {
		a.inReferences[i++] = j;
	    }

	    if (a.inReferences.length == 0)
		inref_zero ++;

	    references = new ArrayList<Integer>();
	    for (i = 0; i < a.outReferences.length; i ++) {
		Integer j = paperIndexMap.get(a.outReferences[i]);
		if (j != null) {
		    references.add(j);
		}
	    }
	    a.outReferences = new int[references.size()];
	    i = 0;
	    for (Integer j : references) {
		a.outReferences[i++] = j;
	    }
	}
	System.out.println("inref zero: " + inref_zero);
	System.out.println("total number of papers: " + documents.size());
		
	return documents;
    }

    public void splitByTrainPercent(double trainPercent) {
	this.trainingSet = this.documents.
	    subList(0, ((int)(documents.size() * trainPercent)));
	this.testingSet = documents.subList(trainingSet.size(),
					    documents.size());
    }

    public Indexer<String> getWordIndexer() {
	return this.wordIndexer;
    }

    public Indexer<PaperAbstract> getPaperIndexer() {
	return this.paperIndexer;
    }
    
    public static double[] evaluate(List<PaperAbstract> testingSet, 
				    Term[] terms, 
				    Integer[][] prediction, 
				    int size, int k, boolean usedWords, 
				    Indexer<String> wordIndexer){

    	if (testingSet.size() != prediction.length)
	    System.out.println("Number of testing docs is not equal to number of documents");

    	int predicted = 0, total = 0;
    	double tfidfScore = 0.0, idfScore = 0.0, idf_top =  Math.log(size);

    	for (int i = 0; i < testingSet.size(); i++) {
	    PaperAbstract doc = testingSet.get(i);
    	    for (int j = 0; j < prediction[i].length && j<k; j++) {
    		Integer wordID = prediction[i][j];
    		if (doc.answerWords.contains(wordID) && 
		    (usedWords || (!doc.modelWords.contains(wordID)))) {
		    
    		    predicted ++;
		    double logVal = (double) (terms[wordID].idfRaw() +
					      (doc.answerWords.
					       contains(wordID) ? 0.0 
					       : 1.0));
		    logVal = Math.log(logVal);

		    tfidfScore += doc.getTf(wordID) * (idf_top - logVal);
    		    idfScore += (idf_top - logVal);
    		}
    		total ++;
    	    }
    	}

	return new double[]{(double)predicted/(double)total,
			    idfScore, tfidfScore}; 
   }

    public static void printResults(double[] results) {
	System.out.println("Predicted: " + results[0]);
	System.out.println("idf score: " + results[1]);
	System.out.println("tfidf score: " + results[2]);
    }

    public static void printResults(File output, double[] results) {
	PlusoneFileWriter writer = new PlusoneFileWriter(output);
	writer.write("Predicted: " + results[0] + "\n");
	writer.write("idf score: " + results[1] + "\n");
	writer.write("tfidf score: " + results[2] + "\n");
	writer.close();
    }

    public void runClusteringMethods(List<PaperAbstract> trainingSet,
				     List<PaperAbstract> testingSet,
				     Term[] terms,
				     File outputDir, int k,
				     boolean usedWord) {
	base = new Baseline(documents, trainingSet, testingSet, 
			    wordIndexer, terms);
	base1 = new Baseline1(documents, trainingSet, testingSet, 
			      wordIndexer, terms);
	
	runClusteringMethod(trainingSet, testingSet, terms, base, outputDir,
			    k, usedWord);
	runClusteringMethod(trainingSet, testingSet, terms, base1, outputDir,
			    k, usedWord);

	int[] closest_k = {1, 3, 5, 10, 25, 50, 100, 
			   250, 500, 1000, 10000, 100000};

	for (int ck = 0; ck < closest_k.length; ck ++) {
	    knn = new KNN(closest_k[ck], trainingSet, testingSet, 
			  wordIndexer, paperIndexer, terms);
	    runClusteringMethod(trainingSet, testingSet, terms, 
				knn, outputDir, k, usedWord);
	}

	int[] dimensions = {10, 20, 50, 100, 150};
	for (int dk = 0; dk < dimensions.length; dk ++) {
	    lsi = new LSI(dimensions[dk], documents,
			  trainingSet, testingSet,
			  wordIndexer, terms);
	    runClusteringMethod(trainingSet, testingSet, terms,
				lsi, outputDir, k, usedWord);
	}
    }

    public void runClusteringMethod(List<PaperAbstract> trainingSet,
				    List<PaperAbstract> testingSet,
				    Term[] terms,
				    ClusteringTest test, File outputDir,
				    int k, boolean usedWord) {
	Integer[][] predict = test.predict(k, usedWord, outputDir);
	double[] result = this.evaluate(testingSet, terms, predict,
					testingSet.size() + 
					trainingSet.size(),
					k, usedWord,
					this.getWordIndexer());
	File out = new File(outputDir, test.testName + ".out");
	Main.printResults(out, result);
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

	String output_folder = args[0];
	String data_file = args[1];

	if (!new File(output_folder).exists()) {
	    new File(output_folder).mkdir();
	}

	if (!new File(data_file).exists()) {
	    System.out.println("Data file does not exist.");
	    System.exit(0);
	}

	Main main = new Main();
	List<PaperAbstract> documents = main.load_data(data_file);
	float trainPercent = new Float(args[2]);

	boolean query = false;

	int[] ks = {1, 5, 10, 15, 20};
	
	if (args.length == 4)
	    query = new Boolean(args[3]);

	if (query) {
	    main.splitByTrainPercent(trainPercent);
	    List<PaperAbstract> trainingSet = main.trainingSet;
	    List<PaperAbstract> testingSet = main.testingSet;	
	
	    Indexer<String> wordIndexer = main.getWordIndexer();
	    Indexer<PaperAbstract> paperIndexer = main.getPaperIndexer();
	    
	    System.out.println("Total number of words: " + wordIndexer.size());
	    System.out.println("Total number of papers: " + paperIndexer.size());

	    double testWordPercent = 0.0;

	    Term[] terms = new Term[wordIndexer.size()];
	    for (int i = 0; i < wordIndexer.size(); i++) {
		terms[i] = new Term(i, wordIndexer.get(i));
	    }
	    
	    for (PaperAbstract a:trainingSet){
		a.generateData(testWordPercent, terms, false);
	    }

	    for (PaperAbstract a:testingSet){
		a.generateData(testWordPercent, null, true);
	    }

	    File twpDir = null;
	    try {
		twpDir = new File(new File(output_folder), testWordPercent + "");
		twpDir.mkdir();
	    } catch(Exception e) {
		e.printStackTrace();
	    }
		
	    for (int ki = 0; ki < ks.length; ki++) {
		int k = ks[ki];

		File kDir = null;
		try {
		    kDir = new File(twpDir, k + "");
		    kDir.mkdir();
		} catch(Exception e) {
		    e.printStackTrace();
		}

		boolean usedWord = false;
		
		System.out.println("!processing testwordpercent: " + testWordPercent + 
				   " k: " + k + " usedWord: " + usedWord);

		File outputDir = null;
		try {
		    outputDir = new File(kDir, usedWord + "");
		    outputDir.mkdir();
		} catch(Exception e) {
		    e.printStackTrace();
		}		    


		main.runClusteringMethods(trainingSet, testingSet, terms,
					  outputDir, k, usedWord);
	    }
	    System.exit(0);
	}
    
	double[] testWordPercents = {0.5};//{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};

	main.splitByTrainPercent(trainPercent);
	List<PaperAbstract> trainingSet = main.trainingSet;
	List<PaperAbstract> testingSet = main.testingSet;	
	
	Indexer<String> wordIndexer = main.getWordIndexer();
	Indexer<PaperAbstract> paperIndexer = main.getPaperIndexer();

	System.out.println("Total number of words: " + wordIndexer.size());
	System.out.println("Total number of papers: " + paperIndexer.size());

	for (int twp = 0; twp < testWordPercents.length; twp++) {
	    double testWordPercent = testWordPercents[twp];

	    Term[] terms = new Term[wordIndexer.size()];
	    for (int i = 0; i < wordIndexer.size(); i++) {
		terms[i] = new Term(i, wordIndexer.get(i));
	    }
	    
	    for (PaperAbstract a:trainingSet){
		a.generateData(testWordPercent, terms, false);
	    }
	    
	    for (PaperAbstract a:testingSet){
		a.generateData(testWordPercent, null, true);
	    }

	    File twpDir = null;
	    try {
		twpDir = new File(new File(output_folder), testWordPercent + "");
		twpDir.mkdir();
	    } catch(Exception e) {
		e.printStackTrace();
	    }
		
	    for (int ki = 0; ki < ks.length; ki++) {
		int k = ks[ki];

		File kDir = null;
		try {
		    kDir = new File(twpDir, k + "");
		    kDir.mkdir();
		} catch(Exception e) {
		    e.printStackTrace();
		}

		boolean usedWord = false;

		System.out.println("processing testwordpercent: " + testWordPercent + 
				   " k: " + k + " usedWord: " + usedWord);

		File outputDir = null;
		try {
		    outputDir = new File(kDir, usedWord + "");
		    outputDir.mkdir();
		} catch(Exception e) {
		    e.printStackTrace();
		}		    


		main.runClusteringMethods(trainingSet, testingSet, terms,
					  outputDir, k, usedWord);
	    }
	}
    }
}
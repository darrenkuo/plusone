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
import plusone.clustering.Lda;
import plusone.clustering.DTRandomWalkPredictor;
import plusone.clustering.KNNRandomWalkPredictor;

import java.io.*;

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

import javax.swing.text.Document;

public class Main {

    private Indexer<String> wordIndexer;
  
    public List<PaperAbstract> load_data(String filename) {
	List<PaperAbstract> results = new ArrayList<PaperAbstract>();

	String index_pattern_string = "#INDEX ([\\d]+)";
	Pattern index_pattern = Pattern.compile(index_pattern_string);

	String inref_pattern_string = "#IN-REF ([\\d\\s]+)";
	Pattern inref_pattern = Pattern.compile(inref_pattern_string);

	String outref_pattern_string = "#OUT-REF ([\\d\\s]+)";
	Pattern outref_pattern = Pattern.compile(outref_pattern_string);

	String abstract_pattern_string = "#ABSTRACT ([\\s\\S]+)";
	Pattern abstract_pattern = Pattern.compile(abstract_pattern_string);

	wordIndexer = new Indexer<String>();

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
		    new PaperAbstract(index, inRef, 
				      outRef, abstractText, 
				      wordIndexer, results.size());
		results.add(a);
	    }
	    br.close();
	} catch(Exception e) {
	    e.printStackTrace();
	}
	    
	return results;
    }

    public Indexer<String> getWordIndexer() {
	return this.wordIndexer;
    }
    
    public static double[] evaluate(List<PaperAbstract> testingSet, 
				    Term[] terms, 
				    Integer[][] prediction, 
				    int size, int k, boolean usedWords, 
				    Indexer<String> wordIndexer){

    	if (testingSet.size() != prediction.length)
	    System.out.println("Number of testing docs is not equal to number of documents");

    	double[] results = new double[3];
    	int predicted = 0, total = 0;
    	double tfidfScore = 0.0, idfScore = 0.0;
    	double idf_top =  Math.log(size);

    	for (int i = 0; i < testingSet.size(); i++) {
	    PaperAbstract doc=testingSet.get(i);
    	    for (int j = 0; j < prediction[i].length && j<k; j++) {
    		Integer wordID = prediction[i][j];
    		if ((doc.predictionWords.contains(wordID)) && 
		    (usedWords || (!doc.outputWords.contains(wordID)))) {
		    
    		    predicted ++;
    		    double temp=(idf_top - 
				 Math.log((double)(terms[wordID].idfRaw() + 
						   (doc.outputWords.
						    contains(wordID)?0:1)))); 

		    tfidfScore += doc.getTf1(wordID) * temp;
    		    idfScore += temp;
    		}
    		total ++;
    	    }
    	}

	//System.out.println("predicted: " + predicted);
	//System.out.println("total: " + total);

	/* FIXME: We probably should divide by k here, rather than the total
	 * number of predictions made; otherwise we reward methods that make
	 * less predictions.  -James */
    	results[0]=((double)predicted)/((double)total);
    	results[1]=idfScore;
    	results[2]=tfidfScore;
    	return results;
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

	float trainPercent = new Float(args[1]);
	//float testWordPercent = new Float(args[2]);
	
	System.out.println("data file " + data_file);
	System.out.println("train percent " + trainPercent);
	//System.out.println("test word percent " + testWordPercent);

	Main main = new Main();

	double[] testWordPercents = {0.1, 0.3, 0.5, 0.7, 0.9};
	int[] ks = {1, 5, 10, 15, 20};
	int[] closest_k = {5, 20, 50, 75, 100, 150, 200};

	List<PaperAbstract> documents = main.load_data(data_file);	
	List<PaperAbstract> trainingSet = 
	    documents.subList(0, ((int)(documents.size() * trainPercent)));
	List<PaperAbstract> testingSet = 
	    documents.subList((int)(documents.size() * trainPercent) + 1,
			      documents.size());
	
	Indexer<String> wordIndexer = main.getWordIndexer();

	System.out.println("Total number of words: " + wordIndexer.size());

	Term[] terms = new Term[wordIndexer.size()];
	for (int i = 0; i < wordIndexer.size(); i++) {
	    terms[i] = new Term(i, wordIndexer.get(i));
	}
	

	for (int twp = 0; twp < testWordPercents.length; twp++) {
	    double testWordPercent = testWordPercents[twp];

	    for (PaperAbstract a:trainingSet){
		a.generateData(testWordPercent, wordIndexer, terms, false);
	    }
	    for (PaperAbstract a:testingSet){
		a.generateData(testWordPercent, wordIndexer, terms, true);
	    }
	    
	    //Lda lda = new Lda(documents, trainingSet, testingSet, wordIndexer, terms);
	    Baseline base = new Baseline(documents, trainingSet, testingSet, wordIndexer, terms);
	    Baseline1 base1 = new Baseline1(documents, trainingSet, testingSet, wordIndexer, terms);

	    File twpDir = null;
	    try {
		twpDir = new File(new File("experiment"), testWordPercent + "");
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

		for (int c = 0; c < 2; c++) {
		    boolean usedWords = true ? c == 0 : false;

		    System.out.println("processing testwordpercent: " + testWordPercent + 
				       " k: " + k + " usedWords: " + usedWords);

		    File outputDir = null;
		    try {
			outputDir = new File(kDir, usedWords + "");
			outputDir.mkdir();
		    } catch(Exception e) {
			e.printStackTrace();
		    }		    

		    /*
		    // Lda
		    Integer[][] LdaPredict = lda.predict(k, usedWords, outputDir);
		    double[] LdaResult = Main.evaluate(testingSet, terms, LdaPredict, 
						       documents.size(), k, usedWords, 
						       main.getWordIndexer());
		    //System.out.println("LDA results");
		    File ldaOut = new File(outputDir, "lda.out");
		    Main.printResults(ldaOut, LdaResult);
		    */
		    for (int ck = 0; ck < closest_k.length; ck ++) {
			int closest_num = closest_k[ck];
			// KNN with citation
			/*
			KNNWithCitation knnc = new KNNWithCitation(closest_num, documents, 
								  trainingSet, testingSet, 
								  wordIndexer, terms);
			
			Integer[][] KNNCPredict = knnc.predict(k, usedWords, outputDir);
			double[] KNNCResult = Main.evaluate(testingSet, terms, KNNCPredict, 
							    documents.size(), k, usedWords, 
							    main.getWordIndexer());
			//System.out.println("KNN results");
			File knncOut = new File(outputDir, "knnc-" + closest_num + ".out");
			Main.printResults(knncOut, KNNCResult);
			*/
			/*
			// knn

			KNN knn = new KNN(closest_num, documents, 
					  trainingSet, testingSet, 
					  wordIndexer, terms);
			
			Integer[][] KNNPredict = knn.predict(k, usedWords, outputDir);
			double[] KNNResult = Main.evaluate(testingSet, terms, KNNPredict, 
							   documents.size(), k, usedWords, 
							   main.getWordIndexer());
			//System.out.println("KNN results");
			File knnOut = new File(outputDir, "knn-" + closest_num + ".out");
			Main.printResults(knnOut, KNNResult);
			*/

                        /*
                        KNNRandomWalkPredictor knnRWPredictor =
                            new KNNRandomWalkPredictor(closest_num, documents,
                                                       trainingSet, testingSet,
                                                       wordIndexer, terms, 1, 0.5, 1);
                        Integer[][] knnRWPredictions = knnRWPredictor.predict(k, usedWords, outputDir);
                        double[] knnRWResult = Main.evaluate(testingSet, terms, knnRWPredictions,
                                                             documents.size(), k, usedWords,
                                                             main.getWordIndexer());
			File knnRWOut = new File(outputDir, "knnrw-" + closest_num + ".out");
			Main.printResults(knnRWOut, knnRWResult);
                        */

		    }
                    ClusteringTest dtRWPredictor =
                        new DTRandomWalkPredictor(documents,
                                                  trainingSet, testingSet,
                                                  wordIndexer, terms,
                                                  1, /* <- walk length */
                                                  300  /* <- num sample walks */);
                    Integer[][] dtRWPredictions = dtRWPredictor.predict(k, usedWords, outputDir);
                    double[] dtRWResult = Main.evaluate(testingSet, terms, dtRWPredictions,
                                                        documents.size(), k, usedWords,
                                                        main.getWordIndexer());
                    File dtRWOut = new File(outputDir, "dtrw.out");
                    Main.printResults(dtRWOut, dtRWResult);

		    // Baseline
		    Integer[][] BLPredict = base.predict(k, usedWords, outputDir);
		    double[] BLResult = Main.evaluate(testingSet, terms, BLPredict, 
						      documents.size(), k, usedWords, 
						      main.getWordIndexer());	
		    //System.out.println("Baseline results");
		    File baseOut = new File(outputDir, "baseline.out");
		    Main.printResults(baseOut, BLResult);

		    // Baseline1
		    Integer[][] BLPredict1 = base1.predict(k, usedWords, outputDir);
		    double[] BLResult1 = Main.evaluate(testingSet, terms, BLPredict1, 
						      documents.size(), k, usedWords, 
						      main.getWordIndexer());	
		    //System.out.println("Baseline results");
		    File baseOut1 = new File(outputDir, "baseline1.out");
		    Main.printResults(baseOut1, BLResult1);
		    base1.reset();
		}
	    }
	}
    }
}
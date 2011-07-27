package plusone.clustering;

import plusone.utils.Indexer;
import plusone.utils.PaperAbstract;
import plusone.utils.PlusoneFileWriter;
import plusone.utils.TFIDFCounter;
import plusone.utils.Utils;
import plusone.utils.WordAndScore;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.ejml.simple.SimpleMatrix;

public class Lda extends ClusteringTest {

    private List<PaperAbstract> documents;
    private Indexer<String> wordIndexer;
    private TFIDFCounter tfidf;
    private static final int CLUSTERS = 50;

    public Lda(List<PaperAbstract> documents, Indexer<String> wordIndexer,
	       TFIDFCounter tfidf) {
	super("Lda");
	this.documents = documents;
	this.wordIndexer = wordIndexer;
	this.tfidf = tfidf;
    }	       

    public void analysis(double trainPercent, double testWordPercent) {
	super.analysis(trainPercent, testWordPercent);

	List<PaperAbstract> trainingSet = 
	    this.documents.subList(0, ((int)(documents.size() * 
					     trainPercent)));
	List<PaperAbstract> testingSet = 
	    this.documents.subList((int)(documents.size() * trainPercent) + 1,
			      documents.size());

	for (PaperAbstract a : testingSet) {
	    a.generateTestset(testWordPercent, this.wordIndexer);
	    //trainingSet.add(a);
	}

	this.train(this.documents, testingSet);
	//this.test(testingSet, testWordPercent);
    }

    private void train(List<PaperAbstract> abstracts, 
		       List<PaperAbstract> testingAbstracts) {
	try {
	    new File("lda").mkdir();
	} catch(Exception e) {
	    e.printStackTrace();
	}

	int k = 5;
	boolean outputUsedWords = false;
	String trainingData = "lda/train.ldain";

	createLdaInput(trainingData, abstracts);
	Utils.runCommand("lib/lda-c-dist/lda est 1 " + CLUSTERS + " lib/lda-c-dist/settings.txt " + trainingData + " random lda", false);

	// new code...
	double[][] betaMatrix = readLdaResultFile("lda/final.beta", 0);
	double[][] gammasMatrix = 
	    readLdaResultFile("lda/final.gamma", abstracts.size() - testingAbstracts.size());

	System.out.println("gammasMatrix size: " + gammasMatrix.length);
	System.out.println("other size: " + gammasMatrix[0].length);

	// matrix multiplication using the EJML package
	SimpleMatrix beta = new SimpleMatrix(betaMatrix);
	SimpleMatrix gammas = new SimpleMatrix(gammasMatrix);
	SimpleMatrix results = gammas.mult(beta);
       
	Integer[][] predictedWords = 
	    this.predictTopKWords(results, testingAbstracts, k, outputUsedWords);

	int predicted = 0, total = 0;
	double tfidfScore = 0.0, idfScore = 0;
	for (int document = 0; document < predictedWords.length; document ++) {
	    //System.out.println("document: " + document + " number of predicted words: " + predictedWords[document].length);
	    for (int predict = 0; predict < predictedWords[document].length; predict ++) {
		Integer wordID = predictedWords[document][predict];
		if (testingAbstracts.get(document).predictionWords.isEmpty())
		    System.out.println("no prediction words in testing set?");

		if (testingAbstracts.get(document).predictionWords
		    .contains(wordID)) {
		    predicted ++;
		    tfidfScore += this.tfidf.tfidf(abstracts.size() - 
						   testingAbstracts.size() + 
						   document, wordID);
		    idfScore += this.tfidf.idf(wordID);
		}

		total ++;
	    }
	}
	System.out.println("Predicted " + ((double)predicted/total)*100 + " percent of the words");
	System.out.println("total attempts: " + total);
	System.out.println("TFIDF score: " + tfidfScore);
	System.out.println("IDF score: " + idfScore);
    }

    private double[][] readLdaResultFile(String filename, int start) {
	List<String[]> gammas = new ArrayList<String[]>();
	double[][] results = null;
	//System.out.println("reading lda results file starting at : " + start);
	try {
	    FileInputStream fstream = new FileInputStream(filename);
	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = 
		new BufferedReader(new InputStreamReader(in));
	    String strLine;

	    int c = 0;
	    while ((strLine = br.readLine()) != null) {
		if (c >= start) {
		    gammas.add(strLine.trim().split(" "));
		}
		c ++;
	    }
	    //System.out.println("C got to " + c);

	    results = new double[gammas.size()][];
	    for (int i = 0; i < gammas.size(); i ++) {
		results[i] = new double[gammas.get(i).length];
		for (int j = 0; j < gammas.get(i).length; j ++) {
		    results[i][j] = new Double(gammas.get(i)[j]);
		}
	    }

	} catch(Exception e) {
	    e.printStackTrace();
	}

	return results;
    }

    private Integer[][] predictTopKWords(SimpleMatrix matrix,
					 List<PaperAbstract> abstracts,
					 int k,
					 boolean outputUsedWords) {
	Integer[][] results = new Integer[abstracts.size()][];
	for (int row = 0; row < matrix.numRows(); row ++) {
	    PriorityQueue<WordAndScore> queue = 
		new PriorityQueue<WordAndScore>();
	    for (int col = 0; col < matrix.numCols(); col ++) {
		queue.add(new WordAndScore(col, matrix.get(row, col), 
					   false));
	    }

	    if (outputUsedWords) {
		results[row] = new Integer[Math.min(k, queue.size())];
		for (int i = 0; i < k && !queue.isEmpty(); i ++) {
		    results[row][i] = queue.poll().wordID;
		}
	    } else {
		//System.out.println("Predicting results for row: " + row);
		List<WordAndScore> lst = new ArrayList<WordAndScore>();
		for (int i = 0; i < k && !queue.isEmpty(); i ++) {
		    WordAndScore cur = queue.poll();
		    //System.out.println("predicted word: " + wordIndexer.get(cur.wordID) + " score: " + cur.score);
		    if (!abstracts.get(row).inferenceWords.contains(cur))
			lst.add(cur);
		    else
			i --;
		}

		results[row] = new Integer[lst.size()];
		for (int i = 0; i < lst.size(); i ++) {
		    results[row][i] = lst.get(i).wordID;
		}
	    }
	}
	return results;
    }

    // helper functions
    private void createLdaInput(String filename, List<PaperAbstract> papers) {

	System.out.println("created lda input in file: " + filename);

	PlusoneFileWriter fileWriter = new PlusoneFileWriter(filename);

	for (PaperAbstract paper : papers) {
	    Map<Integer, Integer> counter =
		new HashMap<Integer, Integer>();
	    for (Integer wordID : paper.outputWords) {
		if (counter.containsKey(wordID))
		    counter.put(wordID, counter.get(wordID) + 1);
		else
		    counter.put(wordID, 1);
	    }

	    fileWriter.write(counter.size() + " ");
	    //ldaInput += ("" + counter.size());
	    for(Map.Entry<Integer, Integer> entry : counter.entrySet())
		//ldaInput += (entry.getKey() + ":" + entry.getValue());
		fileWriter.write(entry.getKey() + ":" + entry.getValue() + " ");

	    fileWriter.write("\n");
	    //ldaInput += "\n";
	}

	fileWriter.close();
    }
}
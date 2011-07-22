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

public class Lda implements ClusteringMethod {

    private List<PaperAbstract> documents;
    private Indexer<String> wordIndexer;
    private TFIDFCounter tfidf;

    public Lda(List<PaperAbstract> documents, Indexer<String> wordIndexer) {
	this.documents = documents;
	this.wordIndexer = wordIndexer;
	this.tfidf = new TFIDFCounter(this.documents, this.wordIndexer);
    }	       

    public void analysis(double trainPercent, double testWordPercent) {
	List<PaperAbstract> trainingSet = 
	    this.documents.subList(0, 
				   ((int)(documents.size() * trainPercent)));
	List<PaperAbstract> testingSet = 
	    this.documents.subList((int)(documents.size() * trainPercent) + 1,
			      documents.size());

	this.train(trainingSet);
	this.test(testingSet, testWordPercent);
    }

    private void train(List<PaperAbstract> abstracts) {
	try {
	    new File("lda").mkdir();
	} catch(Exception e) {
	    e.printStackTrace();
	}

	String trainingData = "lda/train.ldain";
	createLdaInput(trainingData, abstracts);
	Utils.runCommand("lib/lda-c-dist/lda est 1 10 lib/lda-c-dist/settings.txt " + trainingData + " random lda");
    }

    private double[][] readLdaResultFile(String filename) {
	List<String[]> gammas = new ArrayList<String[]>();
	double[][] results = null;
	try {
	    FileInputStream fstream = new FileInputStream(filename);
	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = 
		new BufferedReader(new InputStreamReader(in));
	    String strLine;

	    while ((strLine = br.readLine()) != null) {
		gammas.add(strLine.trim().split(" "));
	    }

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
    
    private void test(List<PaperAbstract> abstracts, double percentUsed) {

	boolean outputUsedWords = false;
	String testingData = "lda/test.ldain";
	for (PaperAbstract a : abstracts) {
	    a.generateTestset(percentUsed, this.wordIndexer);
	}

	createLdaInput(testingData, abstracts);
	Utils.runCommand("lib/lda-c-dist/lda inf lib/lda-c-dist/settings.txt lda/final " + testingData + " lda/lda.test");

	double[][] betaMatrix = readLdaResultFile("lda/final.beta");
	double[][] gammasMatrix = 
	    readLdaResultFile("lda/lda.test-gamma.dat");

	// matrix multiplication using the EJML package
	SimpleMatrix beta = new SimpleMatrix(betaMatrix);
	SimpleMatrix gammas = new SimpleMatrix(gammasMatrix);
	SimpleMatrix results = gammas.mult(beta);

	Integer[][] predictedWords = 
	    this.predictTopKWords(results, abstracts, 1, outputUsedWords);

	int predicted = 0, total = 0;
	double tfidfScore = 0.0, idfScore = 0;
	for (int document = 0; document < predictedWords.length; document ++) {
	    for (int predict = 0; predict < predictedWords[document].length; predict ++) {
		Integer wordID = predictedWords[document][predict];
		if (abstracts.get(document).predictionWords
		    .contains(wordID)) {
		    predicted ++;
		    tfidfScore += this.tfidf.tfidf(document, wordID);
		    idfScore += this.tfidf.idf(wordID);
		}

		total ++;
	    }
	}
	System.out.println("Predicted " + ((double)predicted/total) + " percent of the words");
	System.out.println("TFIDF score: " + tfidfScore);
	System.out.println("IDF score: " + idfScore);

	baselineTest(abstracts, 1, outputUsedWords);
    }

    private void baselineTest(List<PaperAbstract> abstracts, 
			      int k, boolean usedWord) {
	// need global word count
	// somehow get top K words from the global word count

	Integer[][] predictedWords = predictTopKWordsNaive(abstracts, k, 
							   usedWord);
	
	int predicted = 0, total = 0;
	double tfidfScore = 0.0, idfScore = 0;
	for (int document = 0; document < predictedWords.length; document ++) {
	    for (int predict = 0; predict < predictedWords[document].length; predict ++) {
		Integer wordID = predictedWords[document][predict];
		if (abstracts.get(document).predictionWords.contains(wordID)) {
		    predicted ++;
		    tfidfScore += this.tfidf.tfidf(document, wordID);
		    idfScore += this.tfidf.idf(wordID);
		}

		total ++;
	    }
	}

	System.out.println("Predicted " + ((double)predicted/total) + " percent of the words");
	System.out.println("TFIDF score: " + tfidfScore);
	System.out.println("IDF score: " + idfScore);
    }

    private Integer[][] predictTopKWordsNaive(List<PaperAbstract> abstracts,
					  int k, boolean outputUsedWord) {
	Integer[][] results = new Integer[abstracts.size()][];
	for (int a = 0; a < abstracts.size(); a ++) {
	    if (outputUsedWord) {
		results[a] = 
		    new Integer[Math.min(this.tfidf.wordFrequency.length, k)];
		for (int w = 0; w < k && w < this.tfidf.wordFrequency.length; 
		     w ++) {
		    results[a][w] = this.tfidf.wordFrequency[w].wordID;
		}
	    } else {
		int c = 0;
		List<Integer> lst = new ArrayList<Integer>();
		for (int w = 0; c < k && w < this.tfidf.wordFrequency.length;
		     w ++) {
		    Integer curWord = this.tfidf.wordFrequency[w].wordID;
		    if (!abstracts.get(a).
			inferenceWords.contains(curWord)) {
			lst.add(curWord);
			c ++;
		    }
		}
	       
		results[a] = (Integer[])lst.toArray(new Integer[lst.size()]);
	    }
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
		List<WordAndScore> lst = new ArrayList<WordAndScore>();
		for (int i = 0; i < k && !queue.isEmpty(); i ++) {
		    WordAndScore cur = queue.poll();
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
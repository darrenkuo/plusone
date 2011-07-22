package plusone.clustering;

import plusone.utils.Indexer;
import plusone.utils.PaperAbstract;
import plusone.utils.PlusoneFileWriter;
import plusone.utils.Utils;
import plusone.utils.WordAndScore;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.ejml.simple.SimpleMatrix;

public class Lda implements ClusteringMethod {

    private Indexer<String> wordIndexer = new Indexer<String>();

    public void analysis(List<PaperAbstract> documents, double trainPercent,
			 double testWordPercent) {
	List<PaperAbstract> trainingSet = 
	    documents.subList(0, ((int)(documents.size() * trainPercent)));
	List<PaperAbstract> testingSet = 
	    documents.subList((int)(documents.size() * trainPercent) + 1, 
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
	String testingData = "lda/test.ldain";
	for (PaperAbstract a : abstracts) {
	    a.generateTestset(percentUsed);
	}

	createLdaInput(testingData, abstracts);
	Utils.runCommand("lib/lda-c-dist/lda inf lib/lda-c-dist/settings.txt lda/final " + testingData + " lda/lda.test");

	double[][] betaMatrix = readLdaResultFile("lda/final.beta");
	double[][] gammasMatrix = 
	    readLdaResultFile("lda/lda.test-gamma.dat");
	// matrix multiplication
	SimpleMatrix beta = new SimpleMatrix(betaMatrix);
	SimpleMatrix gammas = new SimpleMatrix(gammasMatrix);
	SimpleMatrix results = gammas.mult(beta);

	WordAndScore[][] predictedWords = this.predictTopKWords(results,
								abstracts,
								1, true);

	int predicted = 0, total = 0;
	for (int document = 0; document < predictedWords.length; document ++) {
	    for (int predict = 0; predict < predictedWords[document].length; predict ++) {
		WordAndScore pair = predictedWords[document][predict];
		String word = wordIndexer.get(pair.wordID);
		if (abstracts.get(document).predictionWords.contains(word))
		    predicted ++;
		total ++;
	    }
	}
	System.out.println("Predicted " + ((double)predicted/total) + " percent of the words");
    }

    private WordAndScore[][] predictTopKWords(SimpleMatrix matrix,
					      List<PaperAbstract> abstracts,
					      int k,
					      boolean outputUsedWords) {
	WordAndScore[][] results = new WordAndScore[abstracts.size()][];
	for (int row = 0; row < matrix.numRows(); row ++) {
	    PriorityQueue<WordAndScore> queue = 
		new PriorityQueue<WordAndScore>();
	    for (int col = 0; col < matrix.numCols(); col ++) {
		queue.add(new WordAndScore(col, matrix.get(row, col), 
					   false));
	    }

	    if (outputUsedWords) {
		results[row] = new WordAndScore[Math.min(k, queue.size())];
		for (int i = 0; i < k && !queue.isEmpty(); i ++) {
		    results[row][i] = queue.poll();
		}
	    } else {
		List<WordAndScore> lst = new ArrayList<WordAndScore>();
		for (int i = 0; i < k && !queue.isEmpty(); i ++) {
		    WordAndScore cur = queue.poll();
		    String curWord = wordIndexer.get(cur.wordID);
		    if (!abstracts.get(row).inferenceWords.contains(curWord))
			results[row][i] = cur;
		    else
			i --;
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
	    for (String word : paper.outputWords) {
		int index = wordIndexer.addAndGetIndex(word);

		if (counter.containsKey(index))
		    counter.put(index, counter.get(index) + 1);
		else
		    counter.put(index, 1);
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
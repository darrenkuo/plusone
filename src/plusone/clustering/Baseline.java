package plusone.clustering;

import plusone.utils.Indexer;
import plusone.utils.PaperAbstract;
import plusone.utils.TFIDFCounter;

import java.util.ArrayList;
import java.util.List;

public class Baseline extends ClusteringTest {
    
    private List<PaperAbstract> documents;
    private Indexer<String> wordIndexer;
    private TFIDFCounter tfidf;

    public Baseline(List<PaperAbstract> documents, 
		    Indexer<String> wordIndexer, TFIDFCounter tfidf) {
	super("Baseline");
	this.documents = documents;
	this.wordIndexer = wordIndexer;
	this.tfidf = tfidf;
    }

    @Override
    public void analysis(double trainPercent, double testWordPercent) {
	super.analysis(trainPercent, testWordPercent);

	int k = 5;
	List<PaperAbstract> trainingSet =
	    this.documents.subList(0, ((int)(documents.size() * 
					     trainPercent)));
	List<PaperAbstract> testingSet = 
	    this.documents.subList((int)(documents.size() * 
					 trainPercent) + 1,
			      documents.size());

	for (PaperAbstract a : testingSet) {
	    a.generateTestset(testWordPercent, this.wordIndexer);
	}

	this.baselineTest(testingSet, k, false);
    }

    private void baselineTest(List<PaperAbstract> abstracts, 
			      int k, boolean usedWord) {
	// need global word count
	// somehow get top K words from the global word count

	Integer[][] predictedWords = predictTopKWordsNaive(abstracts, k, 
							   usedWord);
	
	int predicted = 0, total = 0;
	double tfidfScore = 0.0, idfScore = 0.0;
	for (int document = 0; document < predictedWords.length; 
	     document ++) {
	    for (int predict = 0; predict < predictedWords[document].length; 
		 predict ++) {
		Integer wordID = predictedWords[document][predict];
		if (abstracts.get(document).
		    predictionWords.contains(wordID)) {
		    predicted ++;
		    tfidfScore += this.tfidf.tfidf(document, wordID);
		    idfScore += this.tfidf.idf(wordID);
		}
		total ++;
	    }
	}

	System.out.println("Predicted " + ((double)predicted/total) * 100 + 
			   " percent of the words");
	System.out.println("TFIDF score: " + tfidfScore);
	System.out.println("IDF score: " + idfScore);
    }

    private Integer[][] predictTopKWordsNaive(List<PaperAbstract> abstracts,
					      int k, 
					      boolean outputUsedWord) {
	Integer[][] results = new Integer[abstracts.size()][];
	for (int a = 0; a < abstracts.size(); a ++) {
	    if (outputUsedWord) {
		results[a] = 
		    new Integer[Math.min(this.tfidf.
					 wordFrequency.length, k)];
		for (int w = 0; 
		     w < k && w < this.tfidf.wordFrequency.length; 
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
}
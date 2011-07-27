package plusone.clustering;

import plusone.utils.Document;
import plusone.utils.Indexer;
import plusone.utils.KBestList;
import plusone.utils.PaperAbstract;
import plusone.utils.TFIDFCounter;
import plusone.utils.WordAndScore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class KNN extends ClusteringTest {

    private List<PaperAbstract> documents;
    private Indexer<String> wordIndexer;
    private TFIDFCounter tfidf;
    private int k;
    private List<Document> model;

    private int predictK = 5;

    public KNN(int k, List<PaperAbstract> documents, 
	       Indexer<String> wordIndexer, TFIDFCounter tfidf) {
	super("KNN");
	this.documents = documents;
	this.wordIndexer = wordIndexer;
	this.k = k;
	this.tfidf = tfidf;
    }

    public void analysis(double trainPercent, double testWordPercent) {
	super.analysis(trainPercent, testWordPercent);

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
	this.train(trainingSet);
	this.test(trainingSet, testingSet);
    }

    private void train(List<PaperAbstract> training) {
	try {
	    new File("lda").mkdir();
	} catch(Exception e) {
	    e.printStackTrace();
	}

	this.model = new ArrayList<Document>();
	//System.out.println("training...");
	for (PaperAbstract a : training) {
	    Document t = Document.abstractToDocument(a);
	    //System.out.println("t size: " + t.size());
	    this.model.add(t);
	}
	
    }

    private void test(List<PaperAbstract> training,
		      List<PaperAbstract> testing) {
	// TODO: find the k closest neighbors and predict...
	// using fast distance since it's sparse
	// traverse through all the node once only.

	int prediction = 0, total = 0, predictK = 5;
	double tfidfScore = 0.0, idfScore = 0.0;

	for (int document = 0; document < testing.size(); document ++) {
	    KBestList<Document> kList = new KBestList(k);
	    PaperAbstract a = testing.get(document);
	    Document t = Document.abstractToDocument(a);
	    //System.out.println("doc t size: " + t.size());

	    for (Document d : this.model) {
		double dist = t.distance(d);
		//System.out.println("distance: " + dist);
		kList.insert(d, dist);
	    }

	    List<Integer> predictedWords = this.predictTopKWords(kList, a,
								 predictK);
	    for (Integer w : predictedWords) {
		if (a.inferenceWords.contains(w)) {
		    prediction ++;
		    tfidfScore += this.tfidf.tfidf(training.size() + 
						   document, w);
		    idfScore += this.tfidf.idf(w);
		}
		total ++;
	    }
	}

	System.out.println("Predicted " + ((double)prediction/total) * 100 + 
			   " percent of the words");
	System.out.println("TFIDF score: " + tfidfScore);
	System.out.println("IDF score: " + idfScore);
    }

    private List<Integer> predictTopKWords(KBestList<Document> kList, 
					   PaperAbstract testDoc, int k) {
	List<Integer> predictedWords = new ArrayList<Integer>();
	Document d = new Document();
	Iterator<Document> iter = kList.iterator();
	//System.out.println("in predict top k klist size: " + kList.size);
	while (iter.hasNext()) {
	    Document t = iter.next();
	    //System.out.println("number of words: " + t.size());
	    Iterator<Integer> words = t.getWordIterator();
	    while (words.hasNext()) {
		Integer word = words.next();
		d.addWord(word, t.getWordCount(word));
	    }
	}

	//System.out.println("Done adding counts for all the neighbor documents.");

	List<WordAndScore> wordsList = new ArrayList<WordAndScore>();
	Iterator<Integer> wordsIter = d.getWordIterator();
	while (wordsIter.hasNext()) {
	    Integer word = wordsIter.next();
	    wordsList.add(new WordAndScore(word, d.getWordCount(word), 
					   false));
	}
	Collections.sort(wordsList);
	//System.out.println("Sorted word list.");

	for (int i = 0; i < wordsList.size() && i < 20; i ++) {
	    WordAndScore pair = wordsList.get(i);
	    //System.out.println("Word: " + this.wordIndexer.get(pair.wordID) +
	    //" score: " + pair.score);
	}

	for (WordAndScore pair : wordsList) {
	    if (!testDoc.predictionWords.contains(pair.wordID))
		predictedWords.add(pair.wordID);
	    if (predictedWords.size() == k)
		return predictedWords;
	}
	return predictedWords;
    }
}
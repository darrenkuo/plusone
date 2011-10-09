package plusone.clustering;

import plusone.utils.Document;
import plusone.utils.Indexer;
import plusone.utils.KBestList;
import plusone.utils.PaperAbstract;
import plusone.utils.PlusoneFileWriter;
import plusone.utils.Term;
import plusone.utils.WordAndScore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class KNNWithCitation extends KNN {

    public KNNWithCitation(int K_CLOSEST, 
			   List<PaperAbstract> trainingSet,
			   List<PaperAbstract> testingSet,
			   Indexer<String> wordIndexer,
			   Indexer<PaperAbstract> paperIndexer,
			   Term[] terms) {
	super(K_CLOSEST, trainingSet, testingSet, 
	      wordIndexer, paperIndexer, terms);
	this.testName = "knnc";
    }

    public Integer[][] predict(int k, boolean outputUsedWord, 
			       File outputDirectory) {
	Integer[][] array = new Integer[this.testingSet.size()][];
	
	PlusoneFileWriter writer = null;
	if (outputDirectory != null) {
	    writer = new PlusoneFileWriter(new File(outputDirectory, 
						    this.testName + "-" +
						    k + "-" + 
						    outputUsedWord + 
						    ".predict"));
	}
	
	for (int document = 0; document < this.testingSet.size(); 
	     document ++) {
	    PaperAbstract a = testingSet.get(document);
	    /**
	     * First get 1.5 * k nearest neighbor using similarity
	     * function, then rank neighbor by number of in
	     * references. Lastly, take the top k for prediction.
	     */
	    Integer[] kList = this.kNbr(a, (int)(this.K_CLOSEST * 1.5));
	    kList = this.kCitation(a, kList, this.K_CLOSEST);
	    
	    List<Integer> lst = this.predictTopKWordsWithKList(kList, a, k, 
							       outputUsedWord);

	    array[document] = new Integer[lst.size()];
	    for (int i = 0; i < lst.size(); i ++) {
		array[document][i] = lst.get(i);
		if (outputDirectory != null)
		    writer.write(this.wordIndexer.get(array[document][i]) + " " );
	    }
	    //array[document] = (Integer[])lst.toArray(new Integer[lst.size()]);

	    if (outputDirectory != null)
		writer.write("\n");
	}

	if (outputDirectory != null)
	    writer.close();
	return array;

    }

    public Integer[] kCitation(PaperAbstract b, Integer[] lst, 
			       int K_CLOSEST) {
	//System.out.println("kList length: " + lst.length);
	PriorityQueue<WordAndScore> queue = 
	    new PriorityQueue<WordAndScore>(K_CLOSEST + 1);
	for (Integer ID : lst) {
	    //PaperAbstract a = trainingSet.get(ID);
	    PaperAbstract a = this.paperIndexer.get(ID);
	    double count = (double)a.inReferences.length;
	    if (queue.size() < K_CLOSEST || count > queue.peek().score) {
		if (queue.size() >= K_CLOSEST) {
		    WordAndScore i = queue.poll();
		    //System.out.println("dequeuing ID: " + i.wordID + " score: " + i.score + " for ID: " + a.index + " score: " + count);
		}
		queue.add(new WordAndScore(ID, count, true));
	    }
	}

	Integer[] results = new Integer[Math.min(K_CLOSEST, queue.size())];
	for (int i = 0; i < K_CLOSEST && !queue.isEmpty(); i ++) {
	    results[i] = queue.poll().wordID;
	}
	return results;
    }
}
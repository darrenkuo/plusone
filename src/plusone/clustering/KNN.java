package plusone.clustering;

import plusone.Main;

import plusone.utils.Indexer;
import plusone.utils.ItemAndScore;
import plusone.utils.KBestList;
import plusone.utils.PaperAbstract;
import plusone.utils.PlusoneFileWriter;
import plusone.utils.PredictionPaper;
import plusone.utils.Term;
import plusone.utils.TrainingPaper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class KNN extends ClusteringTest {

    protected List<TrainingPaper> trainingSet;
    protected Term[] terms;
    protected int K_CLOSEST;
    
    public KNN(int K_CLOSEST,
	       List<TrainingPaper> trainingSet,
	       Term[] terms) {
	super("knn-" + K_CLOSEST);
	this.K_CLOSEST = K_CLOSEST;
	this.trainingSet = trainingSet;
	this.terms = terms;
    }
    
    @Override
    public Integer[] predict(int k, PredictionPaper testPaper) {
	Integer[] kList = kNbr(testPaper, K_CLOSEST);
	    
	List<Integer> words = 
	    predictTopKWordsWithKList(kList, testPaper, k);
	return words.toArray(new Integer[words.size()]);
    }
    
    protected List<Integer> predictTopKWordsWithKList
	(Integer[] kList, PredictionPaper testDoc, int k) {
	
	int[] count = new int[terms.length];
	List<Integer> wordSet = new ArrayList<Integer>();
	
	for (int i = 0; i < kList.length; i++){
	    Integer paperIndex = kList[i];
	    TrainingPaper a = Main.getPaperIndexer().get(paperIndex);

	    for (Integer word : a.getTrainingWords()) {
		if (count[word] == 0)
		    wordSet.add(word);
		count[word] += a.getTrainingTf(word);
	    }
	}
	
	PriorityQueue<ItemAndScore> queue = 
	    new PriorityQueue<ItemAndScore>(k + 1);
	for (Integer word : wordSet) {
	    if (testDoc.getTrainingTf(word) > 0)
	    	continue;

	    if (queue.size() < k || 
		(double)count[word] > queue.peek().score) {
		if (queue.size() >= k)
		    queue.poll();
		queue.add(new ItemAndScore(word, count[word], true));
	    }
	}
	
	List<Integer> results = new ArrayList<Integer>(queue.size());
	for (int i = 0; i < k && !queue.isEmpty(); i ++) {
	    results.add((Integer)queue.poll().item);
	}
	
	return results;
    }	
        
    /**
     * Gets the k closest neighbors using the similarity function
     * defined in PaperAbstract.
     */
    public Integer[] kNbr(PredictionPaper doc, int K_CLOSEST){
	PriorityQueue<ItemAndScore> queue = 
	    new PriorityQueue<ItemAndScore>(K_CLOSEST + 1);
	
	for (int i = 0; i < trainingSet.size(); i++) {
	    TrainingPaper a = trainingSet.get(i);
	    double sim = ((PaperAbstract)doc).similarity((PaperAbstract)a);
	    
	    if (queue.size() < K_CLOSEST || sim > queue.peek().score) {
		if (queue.size() >= K_CLOSEST) {
		    queue.poll();
		}
		queue.add(new ItemAndScore(Main.getPaperIndexer().
					   fastIndexOf(a), 
					   sim, true));
	    }
	}

	Integer[] results = new Integer[Math.min(K_CLOSEST, queue.size())];
	for (int i = 0; i < K_CLOSEST && !queue.isEmpty(); i ++) {
	    results[i] = ((Integer) queue.poll().item);
	}
	
	return results;
    }
}
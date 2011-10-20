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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class KNNWithCitation extends KNN {

    public KNNWithCitation(int K_CLOSEST, 
			   List<TrainingPaper> trainingSet,
			   Term[] terms) {
	super(K_CLOSEST, trainingSet, terms);
	this.testName = "knnc-" + K_CLOSEST;
    }

    public Integer[] predict(int k, PredictionPaper testPaper) {
	
	/**
	 * First get 2 * k nearest neighbor using similarity
	 * function, then rank neighbor by number of in
	 * references. Lastly, take the top k for prediction.
	     */
	Integer[] kList = kNbr(testPaper, K_CLOSEST);
	Map<Integer, Integer> distance = 
	    this.kCitation(testPaper, kList, K_CLOSEST);
	    
	List<Integer> lst = 
	    this.predictTopKWordsWithKList(kList, distance, testPaper, k);

	return lst.toArray(new Integer[lst.size()]);
    }

    public Map<Integer, Integer> kCitation(PredictionPaper testPaper, 
					   Integer[] lst, int K_CLOSEST) {
	Map<Integer, Integer> distance = new HashMap<Integer, Integer>();
	Queue<Integer> currentQueue = new LinkedList<Integer>();
	Queue<Integer> nextQueue = new LinkedList<Integer>();
	Indexer<PaperAbstract> paperIndexer = Main.getPaperIndexer();

	PaperAbstract b = (PaperAbstract) testPaper;
	for (Integer currentPaper : b.inReferences) {
	    currentQueue.offer(currentPaper);
	}

	for (Integer currentPaper : b.outReferences) {
	    currentQueue.offer(currentPaper);
	}

	int d = 1;
				    
	while (!currentQueue.isEmpty()) {
	    Iterator<Integer> iter = currentQueue.iterator();
	    while (iter.hasNext()) {
		Integer currentPaper = iter.next();
		if (distance.containsKey(currentPaper))
		    continue;
		distance.put(currentPaper, d);
		 
		PaperAbstract paper = paperIndexer.get(currentPaper);
		for (Integer neighbors : paper.inReferences) {
		    nextQueue.offer(neighbors);
		}
		for (Integer neighbors : paper.outReferences) {
		    nextQueue.offer(neighbors);
		}
	    }
	    d ++;
	    currentQueue = nextQueue;
	    nextQueue = new LinkedList<Integer>();
	}

	int maxD = 1;
	for (Integer p : lst) {
	    if (distance.containsKey(p))
		maxD = Math.max(maxD, distance.get(p));
	}

	for (Integer p : lst) {
	    if (!distance.containsKey(p))
		distance.put(p, maxD + 1);
	}

	return distance;
    }

    protected List<Integer> predictTopKWordsWithKList
	(Integer[] kList, Map<Integer, Integer> distance, 
	 PredictionPaper testDoc, int k) {				
	
	double[] count = new double[terms.length];
	List<Integer> wordSet = new ArrayList<Integer>();
	Indexer<PaperAbstract> paperIndexer = Main.getPaperIndexer();
	
	for (int i = 0; i < kList.length; i++){
	    PaperAbstract a = paperIndexer.get(kList[i]);

	    for (Integer word : a.getTrainingWords()) {
		if (count[word] == 0)
		    wordSet.add(word);

		count[word] += a.getTrainingTf(word) * 
		    (1.0/distance.get(paperIndexer.fastIndexOf(a)));
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
	
	List<Integer> results = 
	    new ArrayList<Integer>(Math.min(k, queue.size()));
	for (int i = 0; i < k && !queue.isEmpty(); i ++) {
	    Integer id = (Integer)queue.poll().item;
	    results.add(id);
	}
	
	return results;
    }
}
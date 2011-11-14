package plusone.clustering;

import plusone.Main;
import plusone.utils.Indexer;
import plusone.utils.ItemAndScore;
import plusone.utils.KNNGraphDistanceCache;
import plusone.utils.KNNSimilarityCache;
import plusone.utils.PaperAbstract;
import plusone.utils.PredictionPaper;
import plusone.utils.Terms;
import plusone.utils.TrainingPaper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class CommonNeighbors extends KNNWithCitation {
    
    public CommonNeighbors(int K_CLOSEST, List<TrainingPaper> trainingPapers,
			   Indexer<PaperAbstract> paperIndexer,
			   KNNSimilarityCache similarityCache,
			   KNNGraphDistanceCache graphDistCache,
			   Terms terms) {
	super(K_CLOSEST, trainingPapers, paperIndexer, similarityCache,
	      graphDistCache, terms);
	this.testName = "cn-" + K_CLOSEST;
    }

    public Map<Integer, Double> kCitation(PredictionPaper testPaper, 
					  Integer[] lst, int K_CLOSEST) {
	int c = 5;
	Map<Integer, Double> distance = new HashMap<Integer, Double>();
	for (Integer p : lst) {
	    distance.put(p, c * Math.exp
			 (graphDistCache.getCommonNeighbors
			  (p, testPaper.getIndex())));
	}
	return distance;
    }

    /*
    public Integer[] predict(int k, PredictionPaper testPaper) {
	List<TrainingPaper> lst = new ArrayList<TrainingPaper>();
	for (TrainingPaper trainPaper : trainingPapers) {
	    if (graphDistCache.get(trainPaper.getIndex(),
				   testPaper.getIndex()) == 2)
		lst.add(trainPaper);
	}

	Map<Integer, Double> tf = PaperAbstract.getCombinedTf(lst);
	Queue<ItemAndScore> queue = new PriorityQueue<ItemAndScore>();
	for (Map.Entry<Integer, Double> entry : tf.entrySet()) {
	    if (testPaper.getTrainingTf(entry.getKey()) > 0)
		continue;

	    if (queue.size() >= k)
		queue.poll();

	    queue.offer(new ItemAndScore
			(entry.getKey(), entry.getValue(), true));
	}
	
	Integer[] results = new Integer[queue.size()];
	for (int i = 0; i < results.length; i ++) {
	    results[i] = ((Integer)queue.poll().item);
	}
	return results;
    }
    */
}
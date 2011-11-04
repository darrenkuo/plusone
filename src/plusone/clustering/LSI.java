package plusone.clustering;

import plusone.Main;
import plusone.utils.ItemAndScore;
import plusone.utils.PredictionPaper;
import plusone.utils.SVD;
import plusone.utils.Terms;
import plusone.utils.TrainingPaper;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class LSI extends ClusteringTest {
    public int DIMENSION;
    protected SVD svd;
    protected Terms terms;
    
    public LSI(int DIMENSION, List<TrainingPaper> trainingSet, Terms terms) {
	super("LSI-" + DIMENSION);
	this.DIMENSION = DIMENSION;
	this.terms = terms;
	
	svd = new SVD(DIMENSION, trainingSet, terms);
    }

    public Integer[] predict(int k, PredictionPaper testPaper) {
	Queue<ItemAndScore> queue = new PriorityQueue<ItemAndScore>(k+1);

	double[] dock = svd.reduceToK(testPaper);
	double[] terms = svd.predictTerms(dock);

	for (int i = 0; i < this.terms.size(); i ++) {
	    if (testPaper.getTrainingTf(i) > 0)
		continue;

	    if (queue.size() < k || 
		terms[i] > queue.peek().score) {    
		if (queue.size() >= k)
		    queue.poll();
		queue.add(new ItemAndScore(new Integer(i), terms[i], true));
	    }
	}
	
	Integer[] results = new Integer[Math.min(k, queue.size())];
	for (int i = 0; i < k && !queue.isEmpty(); i ++) {
	    results[i] = (Integer)queue.poll().item;
	}

	return results;
    }
}
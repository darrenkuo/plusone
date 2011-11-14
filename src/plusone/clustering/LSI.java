package plusone.clustering;

import plusone.Main;
import plusone.utils.ItemAndScore;
import plusone.utils.PredictionPaper;
import plusone.utils.SVD;
import plusone.utils.Terms;
import plusone.utils.TrainingPaper;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class LSI extends ClusteringTest {

    class Entry{
	public int docID;
	public int termID;
	public double value;


	public Entry(int docID, int termID, double value) {
	    this.docID = docID;
	    this.termID = termID;
	    this.value = value;
	}
    }
    protected List<TrainingPaper> trainingSet;
    protected LinkedList<Entry>[] DocTerm;
    protected LinkedList<Entry>[] TermDoc;
    protected int DIMENSION;
    protected double[][] mu;
    protected double[][] beta;
    protected double[] sigma;
    protected Terms terms;
    protected SVD svd;
    public int numTerms;
	
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
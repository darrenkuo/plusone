package plusone.clustering;

import plusone.Main;
import plusone.utils.Indexer;
import plusone.utils.ItemAndScore;
import plusone.utils.PaperAbstract;
import plusone.utils.PlusoneFileWriter;
import plusone.utils.PredictionPaper;
import plusone.utils.Term;
import plusone.utils.TrainingPaper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Baseline extends ClusteringTest {
    
    protected Term[] terms;
    protected List<TrainingPaper> trainingSet;

    public Baseline(List<TrainingPaper> trainingSet,
		    Term[] terms) {
	super("Baseline");
	this.terms = terms;
	this.trainingSet = trainingSet;
    }

    @Override
    public Integer[] predict(int k, PredictionPaper testPaper) {
	PriorityQueue<ItemAndScore> sortQueue = 
	    new PriorityQueue<ItemAndScore>();
	
	for (int i = 0; i < terms.length; i ++) {
	    sortQueue.add(new ItemAndScore(new Integer(i), 
					   terms[i].totalCount,
					   false));
	}

	int c = 0;
	Integer[] topWords = new Integer[sortQueue.size()];
	while (!sortQueue.isEmpty()) {
	    ItemAndScore it = sortQueue.poll();
	    topWords[c++] = ((Integer)it.item);
	}

	c = 0;
	List<Integer> lst = new ArrayList<Integer>();
	for (int w = 0; c < k && w < this.terms.length; w ++) {
	    Integer curWord = topWords[w];
	    if (testPaper.getTrainingTf(curWord) == 0) {
		lst.add(curWord);
		c ++;
	    }
	}
	
	return (Integer[])lst.toArray(new Integer[lst.size()]);
    }
}
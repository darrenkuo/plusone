package plusone.clustering;

import plusone.Main;
import plusone.utils.Indexer;
import plusone.utils.ItemAndScore;
import plusone.utils.PaperAbstract;
import plusone.utils.PlusoneFileWriter;
import plusone.utils.PredictionPaper;
import plusone.utils.Terms;
import plusone.utils.TrainingPaper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Baseline extends ClusteringTest {
    
    private List<TrainingPaper> trainingSet;
    private Terms terms;    

    public Baseline(List<TrainingPaper> trainingSet, Terms terms) {
	super("Baseline");
	this.trainingSet = trainingSet;
	this.terms = terms;
    }

    @Override
    public Integer[] predict(int k, PredictionPaper testPaper) {

	List<Integer> lst = new ArrayList<Integer>();
	
	for (Terms.Term curTerm : terms.getSortedTermsIterable()) {
	    Integer curWord = curTerm.id;
	    if (lst.size() >= k) 
		break;

	    if (testPaper.getTrainingTf(curWord) == 0.0)
		lst.add(curWord);
	}
	
	return (Integer[])lst.toArray(new Integer[lst.size()]);
    }
}
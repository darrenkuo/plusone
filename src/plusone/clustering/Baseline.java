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
    
    protected List<TrainingPaper> trainingSet;

    public Baseline(List<TrainingPaper> trainingSet) {
	super("Baseline");
	this.trainingSet = trainingSet;
    }

    @Override
    public Integer[] predict(int k, PredictionPaper testPaper) {

	List<Integer> lst = new ArrayList<Integer>();
	
	for (Term curTerm : Main.getSortedTerms()) {
	    Integer curWord = curTerm.id;
	    if (lst.size() >= k) 
		break;

	    if (testPaper.getTrainingTf(curWord) == 0)
		lst.add(curWord);
	}
	
	return (Integer[])lst.toArray(new Integer[lst.size()]);
    }
}
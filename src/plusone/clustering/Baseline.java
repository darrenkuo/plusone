package plusone.clustering;

import plusone.utils.PredictionPaper;
import plusone.utils.Terms;
import plusone.utils.TrainingPaper;

import java.util.List;

public class Baseline extends ClusteringTest {
    
    private List<TrainingPaper> trainingSet;
    private Terms terms;    

    public Baseline(List<TrainingPaper> trainingSet, Terms terms) {
	super("Baseline");
	this.trainingSet = trainingSet;
	this.terms = terms;
    }

    @Override
    public double[] predict(PredictionPaper testPaper) {
    	double[] ret = new double[terms.size()];
	
	for (int i=0;i<terms.size();i++) {
		int id=terms.get(i).id;
		if (id!=i)
			System.out.println("in Baseline: the id of term is not the same as the index " +
					"of it in the terms array!!");
		double freq = terms.get(i).totalCount;

	    if (testPaper.getTrainingTf(id) != 0.0)
	    	freq=0.0;
	    ret[id]=freq;
	}
	
	return ret;
    }
}
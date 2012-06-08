package plusone.clustering;

import plusone.utils.PredictionPaper;
import plusone.utils.Terms;
import plusone.utils.TrainingPaper;
import plusone.utils.LocalCOSample;

import java.util.List;

public class CO extends ClusteringTest {
    protected List<TrainingPaper> trainingSet;
    LocalCOSample co;
    public CO(int docEnzs, int termEnzs,int dtNs, int tdNs, List<TrainingPaper> trainingSet,Terms terms){

    super("LocalCO");

    co = new LocalCOSample(docEnzs,termEnzs, dtNs, tdNs,trainingSet,terms);
    }
    
    @Override
    public double[] predict(PredictionPaper testPaper) {
	return co.predict(testPaper);
    }
}
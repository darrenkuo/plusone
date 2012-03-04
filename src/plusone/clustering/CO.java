package plusone.clustering;

import plusone.Main;
import plusone.utils.ItemAndScore;
import plusone.utils.PredictionPaper;
import plusone.utils.SVD;
import plusone.utils.Terms;
import plusone.utils.TrainingPaper;
import plusone.utils.LocalCOSample;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;


public class CO extends ClusteringTest {
    protected List<TrainingPaper> trainingSet;
    LocalCOSample co;
    public CO(int docEnzs, int termEnzs,int dtNs, int tdNs, List<TrainingPaper> trainingSet,Terms terms){

    super("LocalCO");

    co = new LocalCOSample(docEnzs,termEnzs, dtNs, tdNs,trainingSet,terms);
    }

    public Integer[] predict(int k, PredictionPaper testPaper) {
	return co.predict(k,testPaper);
    }
}
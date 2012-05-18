package recommend.algorithms;

import java.util.*;

import plusone.utils.PredictionPaper;
import plusone.utils.Terms;
import plusone.utils.TrainingPaper;

import recommend.util.WordIndex;


public class Baseline extends Algorithm {
	/*double[] scores;
	
	public Baseline() {
		super( "Baseline" );
	}
	
	public void train( List<HashMap<Integer,Double>> traindocs ) {
		scores = new double[WordIndex.size()];
		
		for( HashMap<Integer,Double> words : traindocs ) {
			for( int word : words.keySet() ) {
				scores[word] += words.get( word );
			}
		}
	}
	
	public double[] predict( HashMap<Integer,Double> givenwords ) {
		return scores;
	}
	*/
	
	private List<TrainingPaper> trainingSet;
    private Terms terms;    

    public Baseline(List<TrainingPaper> trainingSet, Terms terms) {
    	super("Baseline");
    	this.trainingSet = trainingSet;
    	this.terms = terms;
    }
	
	public double[] predict(int k, PredictionPaper paper) {
		double[] scores = new double[WordIndex.size()];
		for (TrainingPaper t : trainingSet) {
			for (Integer w : t.getTrainingWords()) {
				scores[w] += t.getTrainingTf(w);
			}
		}
		return scores;
	}
	
}

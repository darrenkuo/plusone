package recommend.algorithms;

import java.util.*;

import plusone.utils.PredictionPaper;
import plusone.utils.PaperAbstract;
import plusone.utils.Terms;
import plusone.utils.TrainingPaper;

import recommend.util.WordIndex;

public class UserAverage extends Algorithm {
	/*
	public UserAverage() {
		super( "UserAverage" );
	}
	
	public void train( List<HashMap<Integer,Double>> traindocs ) {
		
	}
	
	public double[] predict( HashMap<Integer,Double> givenwords ) {
		double sum = 0.0;
		
		for( Double value : givenwords.values() ) {
			sum += value;
		}
		
		sum /= givenwords.size();
		double[] scores = new double[WordIndex.size()];
		
		for( int i = 0; i < scores.length; i++ ) {
			scores[i] = sum;
		}
		
		return scores;
	}
	*/
	private List<TrainingPaper> trainingSet;
    private Terms terms;
	
	public UserAverage(List<TrainingPaper> trainingSet, Terms terms) {
    	super("UserAverage");
    	this.trainingSet = trainingSet;
    	this.terms = terms;
	}
	 
	public double[] predict(int k, PredictionPaper paper) {
		int sum = 0;
		
		for( Integer value : ((PaperAbstract) paper).getTestingWords() ) {
			sum += value;
		}
		
		sum /= ((PaperAbstract) paper).getTestingWords().size();
		double[] scores = new double[WordIndex.size()];
		
		for( int i = 0; i < scores.length; i++ ) {
			scores[i] = sum;
		}
		
		return scores;
	}
}

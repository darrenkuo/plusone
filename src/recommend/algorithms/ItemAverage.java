package recommend.algorithms;

import java.util.*;

import plusone.utils.PredictionPaper;
import plusone.utils.Terms;
import plusone.utils.TrainingPaper;

import recommend.util.WordIndex;

public class ItemAverage extends Algorithm {
	/*
	double[] scores;
	
	public ItemAverage() {
		super( "ItemAverage" );
	}
	
	public void train( List<HashMap<Integer,Double>> traindocs ) {
		double avg = 0.0;
		int count = 0;
		scores = new double[WordIndex.size()];
		int[] doccount = new int[WordIndex.size()];
		
		for( HashMap<Integer,Double> user : traindocs ) {
			for( int item : user.keySet() ) {
				scores[item] += user.get( item );
				avg += user.get( item );
				doccount[item]++;
			}
			
			count += user.size();
		}
		
		avg /= count;
		
		for( int i = 0; i < scores.length; i++ ) {
			if( doccount[i] != 0 ) {
				scores[i] /= doccount[i];
			} else {
				scores[i] = avg;
			}
		}
	}
	
	public double[] predict( HashMap<Integer,Double> givenwords ) {
		return scores;
	}
	*/
	
	private List<TrainingPaper> trainingSet;
    private Terms terms;
    private double[] scores;
	
	public ItemAverage(List<TrainingPaper> trainingSet, Terms terms) {
    	super("ItemAverage");
    	this.trainingSet = trainingSet;
    	this.terms = terms;
    	
		int avg = 0;
		int count = 0;
		scores = new double[WordIndex.size()];
		int[] doccount = new int[terms.size()];
		
		for( TrainingPaper t : trainingSet ) {
			for( int item : t.getTrainingWords() ) {
				scores[item] += t.getTrainingTf( item );
				avg += t.getTrainingTf( item );
				doccount[item]++;
			}
			
			count += t.getTrainingWords().size();
		}
		
		avg /= count;
		
		for( int i = 0; i < scores.length; i++ ) {
			if( doccount[i] != 0 ) {
				scores[i] /= doccount[i];
			} else {
				scores[i] = avg;
			}
		}
	}
	
	public double[] predict(int k, PredictionPaper paper) {
		return scores;
	}
}

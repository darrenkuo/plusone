package recommend.algorithms;

import java.util.*;

import recommend.util.WordIndex;

public class ItemAverage extends Algorithm {
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
}

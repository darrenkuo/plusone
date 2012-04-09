package recommend.algorithms;

import java.util.*;

import recommend.util.WordIndex;

public class UserAverage extends Algorithm {
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
}

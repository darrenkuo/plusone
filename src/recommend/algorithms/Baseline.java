package recommend.algorithms;


import java.util.*;

import recommend.util.WordIndex;


public class Baseline extends Algorithm {
	double[] scores;
	
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
}

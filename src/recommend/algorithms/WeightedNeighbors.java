package recommend.algorithms;

import java.util.*;

import recommend.util.WordIndex;

public class WeightedNeighbors extends Algorithm {
	List<HashMap<Integer,Double>> traindocs;
	double[] trainnorms;
	
	public WeightedNeighbors() {
		super( "WeightedNeighbors" );
	}

    public void train( List<HashMap<Integer,Double>> traindocs ) {
    	this.traindocs = traindocs;
    	trainnorms = new double[traindocs.size()];
    	
    	for( int i = 0; i < trainnorms.length; i++ ) {
    		double norm2 = 0;
    		HashMap<Integer,Double> traindoc = traindocs.get( i );
    		
    		for( Integer word : traindoc.keySet() ) {
    			double score = traindoc.get( word );
    			norm2 += score*score;
    		}
    		
    		trainnorms[i] = Math.sqrt( norm2 );
    	}
    }

    public double[] predict( HashMap<Integer,Double> givenwords ) {
    	double[] scores = new double[WordIndex.size()];
    	
    	for( int i = 0; i < traindocs.size(); i++ ) {
    		HashMap<Integer,Double> traindoc = traindocs.get( i );
    		double similarity = similarity( givenwords, i );
    		
    		for( int word : traindoc.keySet() ) {
    			scores[word] += similarity*traindoc.get( word );
    		}
    	}
    	
    	return scores;
    }
	
    private double similarity( HashMap<Integer,Double> words, int doc ) {
		int dp = 0;
		HashMap<Integer,Double> traindoc = traindocs.get( doc );
		
		for( int word : words.keySet() ) {
			if( traindoc.containsKey( word ) ) {
				dp += words.get( word )*traindoc.get( word );
			}
		}
		
		return dp/trainnorms[doc];
    }
}

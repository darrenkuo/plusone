package recommend.algorithms;


import java.util.*;

import recommend.util.WordIndex;

public class KNN extends Algorithm {
	int K;
	
	List<HashMap<Integer,Double>> traindocs;
	double[] trainnorms;
	
	public KNN( int K ) {
		super( "KNN-" + K );
		this.K = K;
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
    		//trainnorms[i] = norm2;
    	}
    }

    public double[] predict( HashMap<Integer,Double> givenwords ) {
    	PriorityQueue<Pair> pq = new PriorityQueue<Pair>();
    	
    	for( int i = 0; i < traindocs.size(); i++ ) {
    		double similarity = similarity( givenwords, i );
    		
    		if( pq.size() < K ) {
    			pq.add( new Pair( i, similarity ) );
    		} else if( similarity > pq.peek().similarity ) {
    			pq.poll();
    			pq.add( new Pair( i, similarity ) );
    		}
    	}
    	
    	double[] scores = new double[WordIndex.size()];
    	
    	while( !pq.isEmpty() ) {
    		Pair p = pq.poll();
    		//System.out.println(p.similarity);
    		HashMap<Integer,Double> traindoc = traindocs.get( p.doc );
    		
    		for( int word : traindoc.keySet() ) {
    			scores[word] += traindoc.get( word );
    		}
    	}
    	
	    return scores;
    }
	
    private double similarity( HashMap<Integer,Double> words, int doc ) {
		int dp = 0;
		HashMap<Integer,Double> traindoc = traindocs.get( doc );
		
		for( int word : words.keySet() )
			if( traindoc.containsKey( word ) )
				dp += words.get( word )*traindoc.get( word );
		
		double norm2 = 0;
		
		for( int word : words.keySet() ) {
			double score = words.get( word );
			norm2 += score*score;
		}
		
		System.out.println(dp/( trainnorms[doc]*Math.sqrt( norm2 ) ));
		return dp/( trainnorms[doc]*Math.sqrt( norm2 ) );
		//return dp/( trainnorms[doc] + norm2 - dp );
    }
    
	private static class Pair implements Comparable<Pair> {
		int doc;
		double similarity;
		
		public Pair( int doc, double similarity ) {
			this.doc = doc;
			this.similarity = similarity;
		}
		
		public int compareTo( Pair p ) {
			return similarity > p.similarity ? 1 : -1;
		}
	}
}

package recommend.algorithms;


import java.util.*;

import recommend.util.WordIndex;


public class WKNNIDF extends Algorithm {
	int K;
	
	List<HashMap<Integer,Double>> traindocs;
	double[] trainnorms;
	double[] idf;
	
	public WKNNIDF( int K ) {
		super( "WKNN+IDF-" + K );
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
    	}
    	
		idf = new double[WordIndex.size()];
		
		for( HashMap<Integer,Double> traindoc : traindocs ) {
			for( int word : traindoc.keySet() ) {
				idf[word]++;
			}
		}
		
		for( int word = 0; word < idf.length; word++ ) {
			idf[word] = Math.log( (double)traindocs.size() / ( 1+idf[word] ) );
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
    		HashMap<Integer,Double> traindoc = traindocs.get( p.doc );
    		
    		for( int word : traindoc.keySet() ) {
    			scores[word] += p.similarity*traindoc.get( word );
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
		
		return dp/( trainnorms[doc]*Math.sqrt( norm2 ) );
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

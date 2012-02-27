package recommend.algorithms;

import java.util.*;

import util.WordIndex;

public class CooccurMax extends Algorithm {
	int[] doccount;
	double[] idf;
	HashMap<Integer,Integer>[] cooccur;
	
	public CooccurMax() {
		super( "CooccurMax" );
	}
	
	public void train( List<HashMap<Integer,Double>> traindocs ) {
		doccount = new int[WordIndex.size()];
		idf = new double[WordIndex.size()];
		
		for( HashMap<Integer,Double> traindoc : traindocs ) {
			for( int word : traindoc.keySet() ) {
				doccount[word]++;
			}
		}
		
		for( int word = 0; word < idf.length; word++ ) {
			idf[word] = Math.log( (double)traindocs.size() / (1 + doccount[word]) );
		}
		
		cooccur = new HashMap[WordIndex.size()];
		
		for( int i = 0; i < cooccur.length; i++ ) {
			cooccur[i] = new HashMap<Integer,Integer>();
		}
		
		for( HashMap<Integer,Double> traindoc : traindocs ) {
			for( int word1 : traindoc.keySet() ) {
				for( int word2 : traindoc.keySet() ) {
					cooccur[word1].put( word2, cooccur[word1].containsKey( word2 ) ? 1 + cooccur[word1].get( word2 ) : 1 );
				}
			}
		}
	}
	
	public double[] predict( HashMap<Integer,Double> givenwords ) {
		double[] scores = new double[WordIndex.size()];
		
		for( int g : givenwords.keySet() ) {
			if( doccount[g] > 5 ) {
				for( int w : cooccur[g].keySet() ) {
					scores[w] = Math.max( (double)cooccur[g].get( w ) / doccount[g], scores[w]);
				}
			}
		}
		
		return scores;
	}
}

package recommend.algorithms;


import java.util.*;

import util.WordIndex;

public class NewAlg1 extends Algorithm {
	HashMap<Integer,Double>[] traindocs;
	int[] doccount;
	HashMap<Integer,Integer>[] cooccur;
	
	public NewAlg1() {
		super( "NewAlg1" );
	}
	
	public void train( List<HashMap<Integer,Double>> traindocs ) {
		this.traindocs = new HashMap[traindocs.size()];
		Iterator<HashMap<Integer,Double>> iter = traindocs.iterator();
		
		for( int i = 0; i < this.traindocs.length; i++ ) {
			this.traindocs[i] = iter.next();
		}
		
		doccount = new int[WordIndex.size()];
		
		for( HashMap<Integer,Double> traindoc : traindocs ) {
			for( int word : traindoc.keySet() ) {
				doccount[word]++;
			}
		}
		
		cooccur = new HashMap[WordIndex.size()];
		
		for( int i = 0; i < cooccur.length; i++ ) {
			cooccur[i] = new HashMap<Integer,Integer>();
		}
		
		for( HashMap<Integer,Double> traindoc : traindocs ) {
			for( int word1 : traindoc.keySet() ) {
				for( int word2 : traindoc.keySet() ) {
					cooccur[word1].put( word2, cooccur[word1].containsKey( word2 ) ? 1+cooccur[word1].get( word2 ) : 1 );
				}
			}
		}
	}
	
	public double[] predict( HashMap<Integer,Double> givenwords ) {
		double[] scores = new double[WordIndex.size()];
		boolean[] arr = new boolean[traindocs.length];
		
		for( int i = 0; i < traindocs.length; i++ ) {
			for( Integer g : givenwords.keySet() ) {
				if( traindocs[i].keySet().contains( g ) ) {
					arr[i] = true;
					break;
				}
			}
		}
		
		for( int w = 0; w < scores.length; w++ ) {
			for( int i = 0; i < traindocs.length; i++ ) {
				if( traindocs[i].keySet().contains( w ) || arr[i] ) {
					scores[w]++;
				}
			}
			
			scores[w] -= doccount[w];
			
			for( Integer g1 : givenwords.keySet() ) {
				scores[w] -= doccount[g1];
			}
			
			for( Integer g : givenwords.keySet() ) {
				if( cooccur[w].containsKey( g ) ) {
					scores[w] += cooccur[w].get( g );
				}
			}
			
			for( Integer g1 : givenwords.keySet() ) {
				for( Integer g2 : givenwords.keySet() ) {
					if( g1 == g2 ) {
						break;
					} else if( cooccur[g1].containsKey( g2 ) ) {
						scores[w] += cooccur[g1].get( g2 );
					}
				}
			}
		}
		
		return scores;
	}
}

package recommend.algorithms;
import recommend.WordIndex;

import java.util.*;

public class Cooccur2SumIDF extends Algorithm {
	static final int MIN = 5;
	HashMap<Long,Integer> doccount;
	HashMap<Long,HashMap<Integer,Integer>> cooccur;
	
	public Cooccur2SumIDF() {
		super( "Cooccur2Sum+IDF" );
	}
	
	public void train( List<HashMap<Integer,Double>> traindocs ) {
		doccount = new HashMap<Long,Integer>();
		
		for( HashMap<Integer,Double> traindoc : traindocs ) {
			for( int w : traindoc.keySet() ) {
				long key = ((long)w << 32) + Integer.MAX_VALUE;
				
				if( doccount.containsKey( key ) ) {
					doccount.put( key, doccount.get( key ) + 1 );
				} else {
					doccount.put( key, 1 );
				}
			}
			
			for( int w1 : traindoc.keySet() ) {
				for( int w2 : traindoc.keySet() ) {
					if( w1 < w2 ) {
						long key = ((long)w1 << 32) + w2;
						
						if( doccount.containsKey( key ) ) {
							doccount.put( key, doccount.get( key ) + 1 );
						} else {
							doccount.put( key, 1 );
						}
					}
				}
			}
		}
		
		cooccur = new HashMap<Long,HashMap<Integer,Integer>>();
		
		for( HashMap<Integer,Double> traindoc : traindocs ) {
			for( int g : traindoc.keySet() ) {
				long key = ((long)g << 32) + Integer.MAX_VALUE;
				
				if( doccount.get( key ) >= MIN ) {
					if( !cooccur.containsKey( key ) ) {
						cooccur.put( key, new HashMap<Integer,Integer>() );
					}
					
					HashMap<Integer,Integer> hm = cooccur.get( key );
					
					for( int w : traindoc.keySet() ) {
						if( hm.containsKey( w ) ) {
							hm.put( w, hm.get( w ) + 1 );
						} else {
							hm.put( w, 1 );
						}
					}
				}
			}
			
			for( int g1 : traindoc.keySet() ) {
				for( int g2 : traindoc.keySet() ) {
					long key = ((long)g1 << 32) + g2;
					
					if( g1 < g2 && doccount.get( key ) >= MIN ) {
						if( !cooccur.containsKey( key ) ) {
							cooccur.put( key, new HashMap<Integer,Integer>() );
						}
						
						HashMap<Integer,Integer> hm = cooccur.get( key );
						
						for( int w : traindoc.keySet() ) {
							if( hm.containsKey( w ) ) {
								hm.put( w, hm.get( w ) + 1 );
							} else {
								hm.put( w, 1 );
							}
						}
					}
				}
			}
		}
	}
	
	public double[] predict( HashMap<Integer,Double> givenwords ) {
		double[] scores = new double[WordIndex.size()];
		
		for( int g1 : givenwords.keySet() ) {
			long key = ((long)g1 << 32) + Integer.MAX_VALUE;
			
			if( cooccur.containsKey( key ) ) {
				double count = doccount.get( key );
				HashMap<Integer,Integer> hm = cooccur.get( key );
				
				for( int w : hm.keySet() ) {
					scores[w] = Math.max( hm.get( w ) / count, scores[w] );
				}
			}
			
			for( int g2 : givenwords.keySet() ) {
				key = ((long)g1 << 32) + g2;
				
				if( g1 < g2 && cooccur.containsKey( key ) ) {
					double count = doccount.get( key );
					HashMap<Integer,Integer> hm = cooccur.get( key );
					
					for( int w : hm.keySet() ) {
						scores[w] = Math.max( hm.get( w ) / count, scores[w] );
					}
				}
			}
		}
		
		for( int w = 0; w < WordIndex.size(); w++ ) {
			scores[w] *= WordIndex.getIDF( w );
		}
		
		return scores;
	}
}

package recommend.algorithms;

import java.util.*;

import plusone.utils.PredictionPaper;
import plusone.utils.Terms;
import plusone.utils.TrainingPaper;

import recommend.util.WordIndex;

import plusone.utils.PaperAbstract;

public class Cooccur2Max extends Algorithm {
	/*
	static final int MIN = 5;
	HashMap<Long,Integer> doccount;
	HashMap<Long,HashMap<Integer,Integer>> cooccur;
	
	public Cooccur2Max() {
		super( "Cooccur2Max" );
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
				
				if( doccount.get( key ) > MIN ) {
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
					
					if( g1 < g2 && doccount.get( key ) > MIN ) {
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
		
		return scores;
	}
	*/
	static final int MIN = 5;
	HashMap<Long,Integer> doccount;
	HashMap<Long,HashMap<Integer,Integer>> cooccur;
	private List<TrainingPaper> trainingSet;
    private Terms terms;
	
	public Cooccur2Max(List<TrainingPaper> trainingSet, Terms terms) {
		super( "Cooccur2Max" );
		this.trainingSet = trainingSet;
    	this.terms = terms;
	}
	
	public double[] predict( int k, PredictionPaper paper ) {
		doccount = new HashMap<Long,Integer>();
		
		for( TrainingPaper t : trainingSet ) {
			for( int w : t.getTrainingWords() ) {
				long key = ((long)w << 32) + Integer.MAX_VALUE;
				
				if( doccount.containsKey( key ) ) {
					doccount.put( key, doccount.get( key ) + 1 );
				} else {
					doccount.put( key, 1 );
				}
			}
			
			for( int w1 : t.getTrainingWords() ) {
				for( int w2 : t.getTrainingWords() ) {
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
		
		for( TrainingPaper t : trainingSet ) {
			for( int g : t.getTrainingWords() ) {
				long key = ((long)g << 32) + Integer.MAX_VALUE;
				
				if( doccount.get( key ) > MIN ) {
					if( !cooccur.containsKey( key ) ) {
						cooccur.put( key, new HashMap<Integer,Integer>() );
					}
					
					HashMap<Integer,Integer> hm = cooccur.get( key );
					
					for( int w : t.getTrainingWords() ) {
						if( hm.containsKey( w ) ) {
							hm.put( w, hm.get( w ) + 1 );
						} else {
							hm.put( w, 1 );
						}
					}
				}
			}
			
			for( int g1 : t.getTrainingWords() ) {
				for( int g2 : t.getTrainingWords() ) {
					long key = ((long)g1 << 32) + g2;
					
					if( g1 < g2 && doccount.get( key ) > MIN ) {
						if( !cooccur.containsKey( key ) ) {
							cooccur.put( key, new HashMap<Integer,Integer>() );
						}
						
						HashMap<Integer,Integer> hm = cooccur.get( key );
						
						for( int w : t.getTrainingWords() ) {
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
		
		//Beginning prediction
		
		double[] scores = new double[WordIndex.size()];
		
		for( int g1 : ((PaperAbstract)paper).getTestingWords() ) {
			long key = ((long)g1 << 32) + Integer.MAX_VALUE;
			
			if( cooccur.containsKey( key ) ) {
				double count = doccount.get( key );
				HashMap<Integer,Integer> hm = cooccur.get( key );
				
				for( int w : hm.keySet() ) {
					scores[w] = Math.max( hm.get( w ) / count, scores[w] );
				}
			}
			
			for( int g2 : ((PaperAbstract)paper).getTestingWords() ) {
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
		
		return scores;
	}

}

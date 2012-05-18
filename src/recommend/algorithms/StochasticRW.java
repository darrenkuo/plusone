package recommend.algorithms;

import java.util.*;

import plusone.utils.PredictionPaper;
import plusone.utils.Terms;
import plusone.utils.TrainingPaper;

import recommend.util.WordIndex;
import plusone.utils.PaperAbstract;


public class StochasticRW extends Algorithm {
	/*
	static final int ITERATIONS = 10000;
	
	int[][] neigh;
	int length;
	
	public StochasticRW( int length ) {
	    super( "StochasticRW-"+length );
	    this.length = length;
    }

    public void train( List<HashMap<Integer,Double>> traindocs ) {
    	LinkedList<Integer>[] ll = new LinkedList[WordIndex.size()];
    	
    	for( int i = 0; i < ll.length; i++ ) {
    		ll[i] = new LinkedList<Integer>();
    	}
    	
	    for( HashMap<Integer,Double> hm : traindocs ) {
	    	for( Integer u : hm.keySet() ) {
	    		for( Integer v : hm.keySet() ) {
	    			ll[u].add( v );
	    		}
	    	}
	    }
	    
	    neigh = new int[ll.length][];
	    
	    for( int i = 0; i < ll.length; i++ ) {
	    	neigh[i] = new int[ll[i].size()];
	    	int j = 0;
	    	
	    	for( int v : ll[i] ) {
	    		neigh[i][j++] = v;
	    	}
	    }
    }

    public double[] predict( HashMap<Integer,Double> givenwords ) {
	    double[] scores = new double[WordIndex.size()];
	    
	    for( int start : givenwords.keySet() ) {
	    	if( neigh[start].length == 0 ) {
	    		continue;
	    	}
	    	
	    	for( int i = 0; i < ITERATIONS; i++ ) {
	    		int u = start;
	    		
	    		for( int j = 0; j < length; j++ ) {
	    			u = neigh[u][(int)(neigh[u].length*Math.random())];
	    		}
	    		
	    		scores[u]++;
	    	}
	    }
	    
	    return scores;
    }
    */
	
	static final int ITERATIONS = 10000;
	
	int[][] neigh;
	int length;
	private List<TrainingPaper> trainingSet;
    private Terms terms;
	
	public StochasticRW( int length, List<TrainingPaper> trainingSet, Terms terms ) {
	    super( "StochasticRW-"+length );
	    this.length = length;
	    this.trainingSet = trainingSet;
    	this.terms = terms;
    }
	
	public double[] predict( int k, PredictionPaper paper ) {
		LinkedList<Integer>[] ll = new LinkedList[terms.size()];
    	
    	for( int i = 0; i < ll.length; i++ ) {
    		ll[i] = new LinkedList<Integer>();
    	}
    	
	    for( TrainingPaper t : trainingSet ) {
	    	for( Integer u : t.getTrainingWords() ) {
	    		for( Integer v : t.getTrainingWords() ) {
	    			ll[u].add( v );
	    		}
	    	}
	    }
	    
	    neigh = new int[ll.length][];
	    
	    for( int i = 0; i < ll.length; i++ ) {
	    	neigh[i] = new int[ll[i].size()];
	    	int j = 0;
	    	
	    	for( int v : ll[i] ) {
	    		neigh[i][j++] = v;
	    	}
	    }
	    
	    //prediction starts here
	    double[] scores = new double[WordIndex.size()];
	    
	    for( int start : ((PaperAbstract)paper).getTestingWords() ) {
	    	if( neigh[start].length == 0 ) {
	    		continue;
	    	}
	    	
	    	for( int i = 0; i < ITERATIONS; i++ ) {
	    		int u = start;
	    		
	    		for( int j = 0; j < length; j++ ) {
	    			u = neigh[u][(int)(neigh[u].length*Math.random())];
	    		}
	    		
	    		scores[u]++;
	    	}
	    }
	    
	    return scores;
	}

}

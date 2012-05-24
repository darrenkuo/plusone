package recommend.algorithms;

import java.util.*;

import plusone.utils.PredictionPaper;
import plusone.utils.Terms;
import plusone.utils.TrainingPaper;

import recommend.Main;
import recommend.util.WordIndex;

import plusone.utils.PaperAbstract;

public class StochasticRWWKNN extends Algorithm {
	/*
	static final int ITERATIONS = 3000;
	
	int[][] neigh;
	double[] docnorm2;
	
	public StochasticRWWKNN() {
	    super( "StochasticRWWKNN" );
    }

    public void train( List<HashMap<Integer,Double>> traindocs ) {
    	LinkedList<Integer>[] ll = new LinkedList[WordIndex.size()+traindocs.size()];
    	docnorm2 = new double[traindocs.size()];
    	int i;
    	
    	for( i = 0; i < ll.length; i++ ) {
    		ll[i] = new LinkedList<Integer>();
    	}
    	
    	i = 0;
	    for( HashMap<Integer,Double> hm : traindocs ) {
	    	double norm = 0.0;
	    	
	    	for( Integer u : hm.keySet() ) {
	    		ll[ll.length-i-1].add( u );
	    		ll[u].add( ll.length-i-1 );
	    		norm += 1.0;
	    	}
	    	
	    	docnorm2[i] = Math.sqrt( norm );
	    	i++;
	    }
	    
	    neigh = new int[ll.length][];
	    
	    for( i = 0; i < ll.length; i++ ) {
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
	    		double factor = neigh[u].length;
	    		u = neigh[u][Main.rand.nextInt( neigh[u].length )];
	    		factor *= neigh[u].length / docnorm2[neigh.length-u-1];
	    		u = neigh[u][Main.rand.nextInt( neigh[u].length )];
	    		scores[u] += factor;
	    	}
	    }
	    
	    return scores;
    }
    */
	
	static final int ITERATIONS = 3000;
	
	int[][] neigh;
	double[] docnorm2;
	private List<TrainingPaper> trainingSet;
    private Terms terms;
	
	public StochasticRWWKNN( List<TrainingPaper> trainingSet, Terms terms ) {
	    super( "StochasticRWWKNN" );
	    this.trainingSet = trainingSet;
    	this.terms = terms;
    	
		LinkedList<Integer>[] ll = new LinkedList[terms.size()+trainingSet.size()];
    	docnorm2 = new double[trainingSet.size()];
    	int i;
    	
    	for( i = 0; i < ll.length; i++ ) {
    		ll[i] = new LinkedList<Integer>();
    	}
    	
    	i = 0;
	    for( TrainingPaper t : trainingSet ) {
	    	double norm = 0.0;
	    	
	    	for( Integer u : t.getTrainingWords() ) {
	    		ll[ll.length-i-1].add( u );
	    		ll[u].add( ll.length-i-1 );
	    		norm += 1.0;
	    	}
	    	
	    	docnorm2[i] = Math.sqrt( norm );
	    	i++;
	    }
	    
	    neigh = new int[ll.length][];
	    
	    for( i = 0; i < ll.length; i++ ) {
	    	neigh[i] = new int[ll[i].size()];
	    	int j = 0;
	    	
	    	for( int v : ll[i] ) {
	    		neigh[i][j++] = v;
	    	}
	    }
    }
	
	public double[] predict( PredictionPaper paper ) {
	    
	    double[] scores = new double[terms.size()];
	    
	    for( int start : ((PaperAbstract)paper).getTestingWords() ) {
	    	if( neigh[start].length == 0 ) {
	    		continue;
	    	}
	    	
	    	for( int j = 0; j < ITERATIONS; j++ ) {
	    		int u = start;
	    		double factor = neigh[u].length;
	    		u = neigh[u][Main.rand.nextInt( neigh[u].length )];
	    		factor *= neigh[u].length / docnorm2[neigh.length-u-1];
	    		u = neigh[u][Main.rand.nextInt( neigh[u].length )];
	    		scores[u] += factor;
	    	}
	    }
	    
	    return scores;
	}

}

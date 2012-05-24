package recommend.algorithms;

import java.util.*;

import plusone.utils.PredictionPaper;
import plusone.utils.Terms;
import plusone.utils.TrainingPaper;

import recommend.Main;
import recommend.util.WordIndex;

import plusone.utils.PaperAbstract;

public class StochasticRWCooccurSum extends Algorithm {
	/*
	static final int ITERATIONS = 3000;
	
	int[][] neigh;
	
	public StochasticRWCooccurSum() {
	    super( "StochasticRWCooccurSum" );
    }

    public void train( List<HashMap<Integer,Double>> traindocs ) {
    	LinkedList<Integer>[] ll = new LinkedList[WordIndex.size()+traindocs.size()];
    	int i;
    	
    	for( i = 0; i < ll.length; i++ ) {
    		ll[i] = new LinkedList<Integer>();
    	}
    	
    	i = 1;
	    for( HashMap<Integer,Double> hm : traindocs ) {
	    	for( Integer u : hm.keySet() ) {
	    		ll[ll.length-i].add( u );
	    		ll[u].add( ll.length-i );
	    	}
	    	
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
	    		u = neigh[u][Main.rand.nextInt( neigh[u].length )];
	    		double factor = neigh[u].length;
	    		u = neigh[u][Main.rand.nextInt( neigh[u].length )];
	    		scores[u] += factor;
	    	}
	    }
	    
	    for( int i = 0; i < scores.length; i++ ) {
	    	scores[i] /= ITERATIONS;
	    	scores[i] *= WordIndex.size();
	    }
	    
	    return scores;
    }
    */
	
	static final int ITERATIONS = 3000;
	
	int[][] neigh;
	private List<TrainingPaper> trainingSet;
    private Terms terms;
	
	public StochasticRWCooccurSum(List<TrainingPaper> trainingSet, Terms terms ) {
	    super( "StochasticRWCooccurSum" );
	    this.trainingSet = trainingSet;
    	this.terms = terms;
    	
		LinkedList<Integer>[] ll = new LinkedList[terms.size()+trainingSet.size()];
    	int i;
    	
    	for( i = 0; i < ll.length; i++ ) {
    		ll[i] = new LinkedList<Integer>();
    	}
    	
    	i = 1;
	    for( TrainingPaper t : trainingSet ) {
	    	for( Integer u : t.getTrainingWords() ) {
	    		ll[ll.length-i].add( u );
	    		ll[u].add( ll.length-i );
	    	}
	    	
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
	    		u = neigh[u][Main.rand.nextInt( neigh[u].length )];
	    		double factor = neigh[u].length;
	    		u = neigh[u][Main.rand.nextInt( neigh[u].length )];
	    		scores[u] += factor;
	    	}
	    }
	    
	    for( int j = 0; j < scores.length; j++ ) {
	    	scores[j] /= ITERATIONS;
	    	scores[j] *= terms.size();
	    }
	    
	    return scores;
	}
}

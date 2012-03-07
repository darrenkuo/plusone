package recommend.algorithms;

import java.util.*;

import recommend.Main;
import recommend.util.WordIndex;

public class StochasticRWCooccurSum extends Algorithm {
	static final int ITERATIONS = 1000;
	
	int[][] neigh;
	int length;
	
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
	    }
	    
	    return scores;
    }
}

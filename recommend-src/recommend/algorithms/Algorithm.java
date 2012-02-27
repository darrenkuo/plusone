package recommend.algorithms;

import java.util.*;

public abstract class Algorithm {
	public String name;
	
	public Algorithm( String name ) {
		this.name = name;
	}
	
	public abstract void train( List<HashMap<Integer,Double>> traindocs );
	public abstract double[] predict( HashMap<Integer,Double> givenwords );
}

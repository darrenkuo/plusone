package recommend.algorithms;

import java.util.*;

import plusone.utils.PaperAbstract;
import plusone.utils.PredictionPaper;
import plusone.utils.Terms;
import plusone.utils.TrainingPaper;

import recommend.util.WordIndex;

public class WeightedNeighbors extends Algorithm {
	/*
	List<HashMap<Integer,Double>> traindocs;
	double[] trainnorms;
	double[] itemAverages;
	
	public WeightedNeighbors() {
		super( "WeightedNeighbors" );
	}

    public void train( List<HashMap<Integer,Double>> traindocs ) {
    	ItemAverage items = new ItemAverage();
    	items.train(traindocs);
    	itemAverages = items.predict(traindocs.get(0));
    	
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
    }

    public double[] predict( HashMap<Integer,Double> givenwords ) {
    	double[] scores = new double[WordIndex.size()];
    	double sumOfSims = 0.0;
    	
    	for( int i = 0; i < traindocs.size(); i++ ) {
    		double similarity = similarity( givenwords, i );
    		sumOfSims += similarity;
    	}
    	for( int i = 0; i < traindocs.size(); i++ ) {
    		HashMap<Integer,Double> traindoc = traindocs.get( i );
    		double similarity = similarity( givenwords, i );

    		for( int word = 0; word < WordIndex.size(); word++ ) {
    			if (traindoc.get(word) != null)
    				scores[word] += similarity*traindoc.get( word )/sumOfSims;
    			else
    				scores[word] += similarity*itemAverages[word]/sumOfSims;
    		}
    	}
    	
    	return scores;
    }
	
    private double similarity( HashMap<Integer,Double> words, int doc ) {
		int dp = 0;
		HashMap<Integer,Double> traindoc = traindocs.get( doc );
		
		for( int word : words.keySet() ) {
			if( traindoc.containsKey( word ) ) {
				dp += words.get( word )*traindoc.get( word );
			}
		}
		return dp/trainnorms[doc];
    }*/
		
	double[] usernorms;
	
	private List<TrainingPaper> trainingSet;
    private Terms terms;
	double[] itemAverages;
	
	public WeightedNeighbors(List<TrainingPaper> trainingSet, Terms terms ) {
		super( "WeightedNeighbors-");
		this.trainingSet = trainingSet;
    	this.terms = terms;
    	
    	usernorms = new double[trainingSet.size()];
    	
    	for( int i = 0; i < usernorms.length; i++ ) {
    		double norm2 = 0;
    		TrainingPaper user = trainingSet.get( i );
    		
    		for( Integer item : user.getTrainingWords() ) {
    			double score = user.getTrainingTf( item );
    			norm2 += score*score;
    		}
    		
    		usernorms[i] = Math.sqrt( norm2 );
    		//trainnorms[i] = norm2;
    	}
	}

    public double[] predict( PredictionPaper paper ) {
    	ItemAverage items = new ItemAverage(trainingSet, terms);
    	itemAverages = items.predict(paper);
    	
    	PriorityQueue<Pair> pq = new PriorityQueue<Pair>();
    	double sumOfSims = 0.0;
    	
    	for( int i = 0; i < trainingSet.size(); i++ ) {
    		double similarity = ((PaperAbstract)trainingSet.get(i)).similarity((PaperAbstract)paper);
    		pq.add( new Pair( i, similarity ) );
    	}
    	
    	for (Iterator<Pair> i = pq.iterator(); i.hasNext();) {
    		sumOfSims += i.next().similarity;
    	}
    	
    	double[] scores = new double[terms.size()];
    	
    	while( !pq.isEmpty() ) {
    		Pair p = pq.poll();
    		TrainingPaper traindoc = trainingSet.get( p.doc );
    		
    		for( int word = 0; word < terms.size(); word++) {
    			if (traindoc.getTrainingTf(word) != null)
    				scores[word] += (p.similarity*traindoc.getTrainingTf( word ))/sumOfSims;
    			else
    				scores[word] += p.similarity*itemAverages[word]/sumOfSims;
    		}
    	}
    	
    	return scores;
    }
	/*
	private double similarity( HashMap<Integer,Double> words, int doc ) {
		int dp = 0;
		HashMap<Integer,Double> traindoc = users.get( doc );
		
		for( int word : words.keySet() )
			if( traindoc.containsKey( word ) )
				dp += words.get( word )*traindoc.get( word );
		
		double norm2 = 0;
		
		for( int word : words.keySet() ) {
			double score = words.get( word );
			norm2 += score*score;
		}
		return dp/( usernorms[doc]*Math.sqrt( norm2 ) );
    }
    */
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

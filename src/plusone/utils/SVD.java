package plusone.utils;

import plusone.Main;
import plusone.utils.TrainingPaper;
import plusone.utils.PredictionPaper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

public class SVD {

	class Entry{
		public int docID;
		public int termID;
		public double value;

		public Entry(int docID, int termID, double value) {
			this.docID = docID;
			this.termID = termID;
			this.value = value;
		}
	}

	protected List<TrainingPaper> trainingSet;
	protected LinkedList<Entry>[] DocTerm;
	protected LinkedList<Entry>[] TermDoc;
	protected int DIMENSION;
	protected double[][] mu;
	protected double[][] beta;
	protected double[] sigma;
	public int numTerms;
	private Random rand=new Random();

	public SVD(int DIMENSION, List<TrainingPaper> trainingSet, int num) {

		this.DIMENSION = DIMENSION;
		this.trainingSet = trainingSet;
		numTerms = num;

		long t1 = System.currentTimeMillis();
		System.out.println("[SVD] training with " + DIMENSION + 
				" dimension.");

		mu = new double[DIMENSION][trainingSet.size()];
		beta = new double[DIMENSION][numTerms];
		sigma = new double[DIMENSION];
		DocTerm = new LinkedList[trainingSet.size()];
		TermDoc = new LinkedList[numTerms];
		for (int i = 0; i < trainingSet.size(); i ++) {
			TrainingPaper doc = trainingSet.get(i);
			DocTerm[i] = new LinkedList<Entry>();

			for (Integer word : doc.getTrainingWords()) {
				Entry temp = new Entry(i, word, doc.getTrainingTf(word));
				DocTerm[i].add(temp);
				if (TermDoc[word] == null){
					TermDoc[word] = new LinkedList<Entry>();
				}
				TermDoc[word].add(temp);
			}
		}

		this.train();

		System.out.format("[SVD]] took %.3f seconds.\n",
				(System.currentTimeMillis() - t1)/1000.0);
	}

/*	private Map<Integer, Double> getReducedDocument(int index) {
		Map<Integer, Double> result = new HashMap<Integer, Double>();
		for (int i = 0; i < DIMENSION; i ++) {
			double tf = mu[i][index];
			if (tf != 0.0) {
				result.put(i, tf);
			}
		}
		return result;
	}*/

	public double dotProduct(double[] a, double[] b){
		double result = 0.0;
		for (int i = 0; i < a.length; i ++){
			result += a[i] * b[i];
		}
		return result;
	}

	public void powerMethod(double[] x, double[] y, int k){
		for (int j = 0; j < y.length; j ++)
			y[j] = 1.0/Math.sqrt(y.length);

		double xnorm;
		double ynorm;
		double diff = dotProduct(x, x) * dotProduct(y, y);

		boolean converge = false;
		while (!converge){
			ynorm = dotProduct(y, y);

			if (ynorm <= 0.0001)
				break;

			double[] subtract = new double[k+1];
			for (int i = 0; i < k; i ++){
				subtract[i] = dotProduct(beta[i], y);
			}
			for (int i = 0; i < x.length; i ++){
				double value = 0;
				for (Entry t : DocTerm[i]) {
					value += t.value * y[t.termID];
				}
				for (int j = 0; j < k; j ++)
					value -= mu[j][i] * sigma[j] * subtract[j];
				x[i] = value / ynorm;
			}

			xnorm = dotProduct(x, x);
			if (xnorm <= 0.0001)
				break;

			for (int i = 0; i < k; i ++)
				subtract[i] = dotProduct(mu[i], x);

			for (int i = 0; i < y.length; i ++){
				double value = 0;
				if (TermDoc[i] != null) {
					for (Entry t : TermDoc[i]) {
						value += t.value * x[t.docID];
					}
				}
				for (int j = 0; j < k; j ++)
					value -= beta[j][i] * sigma[j] * subtract[j];

				y[i] = value / xnorm;
			}

			double temp = dotProduct(x, x) * dotProduct(y, y);
			if (Math.abs(diff - temp) < .00001 * diff)
				converge = true;
			diff = temp;
		}

	}

	public void orthog(double[] x1, double[] x2) {
		double length = 0;
		for (int i = 0; i < x1.length; i ++)
			length += x1[i] * x2[i];

		for (int i = 0; i < x2.length; i ++)
			x2[i] -= length * x1[i];
	}

	public double normalize(double[] x) {
		double lengthx = 0;
		for (int i = 0; i < x.length; i ++)
			lengthx += x[i] * x[i];
		lengthx = Math.sqrt(lengthx);

		for (int i = 0; i < x.length; i ++)
			x[i] /= lengthx;

		return lengthx;
	}

	public void train(){
		for (int k = 0; k < DIMENSION; k ++){
			//start with random vector
			for(int i=0;i<mu[k].length;i++)
				mu[k][i]=rand.nextDouble();
			//make it orthogonal to previous vectors
			for (int i = 0; i < k; i ++) {
				orthog(mu[i],  mu[k]);
			}
			powerMethod(mu[k], beta[k], k);

			sigma[k] = 1;
			sigma[k]*=normalize(mu[k]);
			sigma[k]*=normalize(beta[k]); 
		}
	}


	private double similarity(int docId, int termId) {
		double result = 0;
		for (int i = 0; i < DIMENSION; i ++)
			result += mu[i][docId] * sigma[i] * beta[i][termId];
		return result;
	}

	public Integer[] predict(int k, PredictionPaper testPaper) {
		PriorityQueue<ItemAndScore> queue = 
				new PriorityQueue<ItemAndScore>(k+1);

		double[] doct = new double[numTerms];
		for (Integer word : testPaper.getTrainingWords()) {
			doct[word] = testPaper.getTrainingTf(word);
		}

		double[] dock = new double[DIMENSION];
		for (int i = 0; i < dock.length; i ++) {
			dock[i] = dotProduct(doct, beta[i]) / sigma[i];	    
		}

		for (int i = 0; i < numTerms; i ++) {
			if (testPaper.getTrainingTf(i) > 0)
				continue;
			double score = 0.0;
			for (int j = 0; j < DIMENSION; j ++) {
				score += dock[j] * beta[j][i];
			}
			//System.out.println("score: " + score);
			if (queue.size() < k || score > queue.peek().score) {	    
				if (queue.size() >= k)
					queue.poll();
				queue.add(new ItemAndScore(new Integer(i), score, true));
			}
		}

		Integer[] results = new Integer[Math.min(k, queue.size())];
		for (int i = 0; i < k && !queue.isEmpty(); i ++) {
			results[i] = (Integer)queue.poll().item;
		}

		return results;
	}
}
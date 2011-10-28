package plusone.clustering;

import plusone.Main;
import plusone.utils.Indexer;
import plusone.utils.ItemAndScore;
import plusone.utils.PaperAbstract;
import plusone.utils.PlusoneFileWriter;
import plusone.utils.PredictionPaper;
import plusone.utils.TrainingPaper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.HashMap;
import java.util.LinkedList;

public class LSI extends ClusteringTest {
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
	
    public LSI(int DIMENSION, List<TrainingPaper> trainingSet) {
	super("LSI-" + DIMENSION);
	this.DIMENSION = DIMENSION;
	this.trainingSet = trainingSet;
	
	long t1 = System.currentTimeMillis();
	System.out.println("[" + testName + "] training with SVD");
	
	numTerms=Main.getTerms().length;
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

	System.out.format("[" + testName + 
			  "] took %.3f seconds.\n",
			  (System.currentTimeMillis() - t1)/1000.0);	
    }
    public double evalDiff(double[] x, double[] y){
	double result = 0.0;
	for (int i = 0;i < DocTerm.length; i ++) {
	    for (Entry t : DocTerm[i]) {
		result += Math.pow(t.value - x[t.docID] * y[t.termID], 2);
	    }
	}
	return result;
    }
    
    public double dotProduct(double[] a, double[] b){
	double result = 0.0;
	for (int i = 0; i < a.length; i ++){
	    result += a[i] * b[i];
	}
	return result;
    }
    
    public void Iterate(double[] x, double[] y, int k){
	for (int i = 0; i < x.length; i ++)
	    x[i] = 1.0;
	for (int j = 0; j < y.length; j ++)
	    y[j] = 1.0;
	
	double xnorm;
	double ynorm;
	double diff = dotProduct(x, x) * dotProduct(y, y);
	int rounds = 0;
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
	    rounds ++;

	    double temp = dotProduct(x, x) * dotProduct(y, y);
	    //  System.out.println(Math.abs(diff - temp) * 100);
	    if (Math.abs(diff - temp) < .00001 * diff)
		converge = true;
	    diff = temp;
	}
	//System.out.println(rounds);
    }
    
    /*   public void subtract(double[] x, double[] y){
	 for (int i=0;i<DocTerm.length;i++)
	     for (Entry t:DocTerm[i]){
	     t.value-=x[t.docID]*y[t.termID];
	         }
		 }*/
    public void orthog(double[] x1, double[] y1, double[] x2, double[] y2) {
	double length = 0;
	for (int i = 0; i < x1.length; i ++)
	    length += x1[i] * x2[i];

	for (int i = 0; i < x2.length; i ++)
	    x2[i] -= length * x1[i];
	length = 0;

	for (int i = 0; i < y1.length; i ++)
	    length += y1[i] * y2[i];

	for (int i = 0; i < y2.length; i ++)
	    y2[i] -= length * y1[i];
    }

    public double normalize(double[] x, double[] y) {
	double lengthx = 0;
	for (int i = 0; i < x.length; i ++)
	    lengthx += x[i] * x[i];
	lengthx = Math.sqrt(lengthx);

	for (int i = 0; i < x.length; i ++)
	    x[i] /= lengthx;

	double lengthy = 0;
	for (int i = 0; i < y.length; i ++)
	    lengthy += y[i] * y[i];
	lengthy = Math.sqrt(lengthy);

	for (int i = 0; i < y.length; i ++)
	    y[i] /= lengthy;
	return lengthx * lengthy;
    }

    public void train(){
	for (int k = 0; k < DIMENSION; k ++){
	    Iterate(mu[k], beta[k], k);
	    for (int i = 0; i < k; i ++) {
		orthog(mu[i], beta[i], mu[k], beta[k]);
	    }
	    //    subtract(mu[k],beta[k]);
	    sigma[k] = normalize(mu[k], beta[k]); 
	}
	//System.out.println(mu[0][0]+" "+mu[0][1]+" "+beta[0][0]+" "+beta[0][1]+" "+mu[1][0]+" "+mu[1][1]+" "+beta[1][0]+" "+beta[1][1]+" "+sigma[0]+" "+sigma[1]);
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
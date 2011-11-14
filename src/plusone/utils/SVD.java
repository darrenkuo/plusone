package plusone.utils;

import plusone.Main;
import plusone.utils.Terms;
import plusone.utils.TrainingPaper;
import plusone.utils.PredictionPaper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    protected Terms terms;
    protected LinkedList<Entry>[] DocTerm;
    protected LinkedList<Entry>[] TermDoc;
    protected int DIMENSION;
    protected double[][] mu;
    protected double[][] beta;
    protected double[] sigma;

    public SVD(int DIMENSION, List<TrainingPaper> trainingSet, Terms terms) {

	this.DIMENSION = DIMENSION;
	this.trainingSet = trainingSet;
	this.terms = terms;
	
	long t1 = System.currentTimeMillis();
	System.out.println("[SVD] training with " + DIMENSION + 
			   " dimension.");

	mu = new double[DIMENSION][trainingSet.size()];
	beta = new double[DIMENSION][terms.size()];
	sigma = new double[DIMENSION];
	DocTerm = new LinkedList[trainingSet.size()];
	TermDoc = new LinkedList[terms.size()];
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
	train();
	
	System.out.format("[SVD]] took %.3f seconds.\n",
			  (System.currentTimeMillis() - t1)/1000.0);
    }

    private Map<Integer, Double> getReducedDocument(int index) {
	Map<Integer, Double> result = new HashMap<Integer, Double>();
	for (int i = 0; i < DIMENSION; i ++) {
	    double tf = mu[i][index];
	    if (tf != 0.0) {
		result.put(i, tf);
	    }
	}
	return result;
    }

    private double dotProduct(double[] a, double[] b){
	double result = 0.0;
	for (int i = 0; i < a.length; i ++){
	    result += a[i] * b[i];
	}
	return result;
    }
    
    private void Iterate(double[] x, double[] y, int k){
	for (int i = 0; i < x.length; i ++)
	    x[i] = 1.0;
	for (int j = 0; j < y.length; j ++)
	    y[j] = 1.0;
	
	double xnorm, ynorm, diff = dotProduct(x, x) * dotProduct(y, y);
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
	    if (Math.abs(diff - temp) < .00001 * diff)
		converge = true;
	    diff = temp;
	}
    }
    
    private void orthog(double[] x1, double[] y1, double[] x2, double[] y2) {
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

    private double normalize(double[] x, double[] y) {
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

    private void train(){
	for (int k = 0; k < DIMENSION; k ++){
	    Iterate(mu[k], beta[k], k);
	    for (int i = 0; i < k; i ++) {
		orthog(mu[i], beta[i], mu[k], beta[k]);
	    }
	    sigma[k] = normalize(mu[k], beta[k]); 
	}
    }

    private double similarity(int docId, int termId) {
	double result = 0;
	for (int i = 0; i < DIMENSION; i ++)
	    result += mu[i][docId] * sigma[i] * beta[i][termId];
	return result;
    }

    public double[] reduceToK(PredictionPaper testPaper) {
	double[] doct = new double[terms.size()];
	for (Integer word : testPaper.getTrainingWords()) {
	    doct[word] = testPaper.getTrainingTf(word);
	}
	
	double[] dock = new double[DIMENSION];
	for (int i = 0; i < dock.length; i ++) {
	    dock[i] = dotProduct(doct, beta[i]) / sigma[i];	    
	}
	return dock;
    }

    public double[] predictTerms(double[] dock) {
	double[] terms = new double[this.terms.size()];
	for (int i = 0; i < terms.length; i ++) {
	    double score = 0.0;
	    for (int j = 0; j < DIMENSION; j ++) {
		score += dock[j] * beta[j][i];
	    }
	    terms[i] = score;
	}
	return terms;	
    }
}
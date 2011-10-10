package plusone.clustering;

import plusone.utils.Document;
import plusone.utils.Indexer;
import plusone.utils.PaperAbstract;
import plusone.utils.PlusoneFileWriter;
import plusone.utils.Term;
import plusone.utils.WordAndScore;

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

	public Entry(int docID, int termID, double y){
	    this.docID=docID;
	    this.termID=termID;
	    value=y;
	}
    }
    protected List<PaperAbstract> documents;
    protected List<PaperAbstract> trainingSet;
    protected List<PaperAbstract> testingSet;
    protected Indexer<String> wordIndexer;
    protected LinkedList<Entry>[] DocTerm;
    protected LinkedList<Entry>[] TermDoc;
    protected Term[] terms;
    protected int DIMENSION;
    protected double[][] mu;
    protected double[][] beta;
    protected double[] sigma;
    
    public LSI(int DIMENSION,
	       List<PaperAbstract> documents,
	       List<PaperAbstract> trainingSet,
	       List<PaperAbstract> testingSet,
	       Indexer<String> wordIndexer,
	       Term[] terms) {
	super("LSI-" + DIMENSION);
	this.DIMENSION = DIMENSION;
	this.documents = documents;
	this.trainingSet = trainingSet;
	this.testingSet = testingSet;
	this.wordIndexer = wordIndexer;
	this.terms = terms;
	mu = new double[DIMENSION][documents.size()];
	beta = new double[DIMENSION][terms.length];
	sigma = new double[DIMENSION];
	DocTerm = new LinkedList[documents.size()];
	TermDoc = new LinkedList[terms.length];
	for (int i = 0; i < documents.size(); i ++){
	    PaperAbstract doc = documents.get(i);
	    DocTerm[i] = new LinkedList<Entry>();

	    Set<Map.Entry<Integer, Integer>> words = 
		doc.trainingTf.entrySet();

	    Iterator<Map.Entry<Integer,Integer>> iterator = words.iterator();

	    while (iterator.hasNext()){
		Map.Entry<Integer, Integer> entry = iterator.next();
		int key = entry.getKey();
		int cnt = entry.getValue();
		Entry temp = new Entry(i,key,cnt);
		DocTerm[i].add(temp);
		if (TermDoc[key] == null){
		    TermDoc[key] = new LinkedList<Entry>();
		}
		TermDoc[key].add(temp);
	    }
	}
	/*
	  this.DIMENSION=2;
	  mu=new double[2][2];
	  beta=new double[2][3];
	  sigma=new double[2];
	  DocTerm= new LinkedList[2];
	  TermDoc=new LinkedList[3];
	  for (int i=0;i<2;i++){
	      DocTerm[i]=new LinkedList<Entry>();
	          TermDoc[i]=new LinkedList<Entry>();}
		  TermDoc[2]=new LinkedList<Entry>();
		  Entry temp = new Entry(0,0,9);
		  DocTerm[0].add(temp);
		  TermDoc[0].add(temp);
		  temp=new Entry(0,1,0);
		  DocTerm[0].add(temp);
		  TermDoc[1].add(temp);
		  temp=new Entry(1,0,0);
		  DocTerm[1].add(temp);
		  TermDoc[0].add(temp);
		  temp=new Entry(1,1,8);
		  DocTerm[1].add(temp);
		  TermDoc[1].add(temp);
		  temp=new Entry(0,2,7);
		  DocTerm[0].add(temp);
		  TermDoc[2].add(temp);
		  temp=new Entry(1,2,15);
		  DocTerm[1].add(temp);
		  TermDoc[2].add(temp);*/
	this.train();
    }
    public double evalDiff(double[] x, double[] y){
	double result=0;
	for (int i = 0;i < DocTerm.length; i ++) {
	    for (Entry t : DocTerm[i]) {
		result += Math.pow(t.value - x[t.docID] * y[t.termID], 2);
	    }
	}
	return result;
    }
    
    public double dotProduct(double[] a, double[] b){
	double result = 0;
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
    public void orthog(double[] x1, double[] y1, double[] x2, double[] y2){
	double length = 0;
	for (int i = 0; i < x1.length; i ++)
	    length += x1[i] * x2[i];

	for (int i = 0; i < x2.length; i ++)
	    x2[i] -= length * x1[i];
	length=0;

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

    private Integer[] predict(int k, boolean outputUsedWord,
			      int Id, File outputDirectory){
	PriorityQueue<WordAndScore> queue = 
	    new PriorityQueue<WordAndScore>(k+1);
	for (int j = 0; j < terms.length; j ++) {
	    if (!outputUsedWord && documents.get(Id).getModelTf(j) > 0)
		continue;
	    double score = similarity(Id,j);
	    if (queue.size() < k || 
		score > queue.peek().score){
		if (queue.size() >= k)
		    queue.poll();
		queue.add(new WordAndScore(j, score, true));
	    }
	}

	Integer[] results = new Integer[Math.min(k, queue.size())];
	for (int i = 0; i < k && !queue.isEmpty(); i ++) {
	    Integer wordID = queue.poll().wordID;
	    results[i] = wordID; 
	}

	return results;
    }

    public Integer[][] predict(int k, boolean outputUsedWord,
			       File outputDirectory) {
	Integer[][] result = new Integer[this.testingSet.size()][];
	for (int i = trainingSet.size(); i < documents.size(); i ++){
	    result[i - trainingSet.size()] = 
		predict(k, outputUsedWord, i ,outputDirectory);
	}
	return result;
    }
}
package plusone.utils;

import plusone.Main;
import plusone.utils.TrainingPaper;
import plusone.utils.PredictionPaper;
import plusone.utils.Terms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

public class LocalCOSample {

    /*    class Entry{
	public int docID;
	public int termID;
	public double value;

	public Entry(int docID, int termID, double value) {
	    this.docID = docID;
	    this.termID = termID;
	    this.value = value;
	}
	}*/

    protected List<TrainingPaper> trainingSet;
    protected Map<Integer, Double>[] DocTermB, TermDocB;
    protected Map<Integer, Double>[] COSample;
    public int numTerms;
    private Random rand=new Random();

    int docEnzs;
    int termEnzs;
    int dtNs;
    int tdNs;
    Terms terms;

    public LocalCOSample(int docEnzs, int termEnzs, int dtNs, int tdNs,
		       List<TrainingPaper> trainingSet, Terms terms) {

	this.docEnzs = docEnzs;
	this.termEnzs = termEnzs;
	this.dtNs = dtNs;
	this.tdNs = tdNs;

	this.trainingSet = trainingSet;
	numTerms = terms.size();
	this.terms=terms;

	long t1 = System.currentTimeMillis();
	System.out.println("[LocalCOSample] constructor");

	DocTermB = new Map[trainingSet.size()];
        for (int i = 0; i < DocTermB.length; ++i) DocTermB[i] = new HashMap<Integer, Double>();
	TermDocB = new Map[numTerms];
        for (int i = 0; i < TermDocB.length; ++i) TermDocB[i] = new HashMap<Integer, Double>();
	for (int i = 0; i < trainingSet.size(); i ++) {
	    TrainingPaper doc = trainingSet.get(i);

	    for (Integer word : doc.getTrainingWords()) {
		addEntry(DocTermB[i], word, doc.getTrainingTf(word));
		addEntry(TermDocB[word], i, doc.getTrainingTf(word));
	    }
	}

	this.train();

	System.out.format("[LocalCOSample]] took %.3f seconds.\n",
			  (System.currentTimeMillis() - t1)/1000.0);
    }

    public double dotProduct(double[] a, double[] b){
	double result = 0.0;
	for (int i = 0; i < a.length; i ++){
	    result += a[i] * b[i];
	}
	return result;
    }

    public double sparseDot(Map<Integer,Double> x, Map<Integer,Double> y)
    {
	double result = 0.0;

	for (Map.Entry<Integer, Double> entry : x.entrySet())
	    {
		Double y_entry;
		if ((y_entry = y.get(entry.getKey())) != null)
		    {
			result += y_entry*entry.getValue();
		    }
	    }
	return result;
    }

    void addEntry(Map<Integer, Double> x, int key, double value) {
	if (x.get(key) == null)
	    x.put(key, value);
	else
	    x.put(key, x.get(key) + value);
    }

    double l1Norm(Map<Integer, Double> x) {
	double ret = 0;
	for (Map.Entry<Integer, Double> e : x.entrySet()) ret += Math.abs(e.getValue());
	return ret;
    }

    double l2Norm(Map<Integer, Double> x) {
	double ret2 = 0;
	for (Map.Entry<Integer, Double> e : x.entrySet()) ret2 +=e.getValue() * e.getValue();
	return Math.sqrt(ret2);
    }

    public Map<Integer, Double> walkOneWay(Map<Integer,Double> x, int neighbors, int enz, Map<Integer, Double>[] A)
    {
	Map<Integer, Double> step = new HashMap<Integer, Double>();
	for (Map.Entry<Integer, Double> xEntry : x.entrySet())
	    {
		Map<Integer, Double> connected = A[xEntry.getKey()];
		for (Map.Entry<Integer, Double> cEntry : connected.entrySet()) {
		    // This should be an unbiased estimator for the cEntry.key()-th entry of (row xEntry.key() of A) * xEntry.value()
		    double p = Math.min(1.0, neighbors * 1.0 / connected.size());
		    if (rand.nextDouble() <= p)
			addEntry(step, cEntry.getKey(), cEntry.getValue() * xEntry.getValue() / p);
		}
	    }
	Map<Integer, Double> ret = new HashMap<Integer, Double>();
	double l1 = l1Norm(step);
	for (Map.Entry<Integer, Double> sEntry : step.entrySet()) {
	    double p = (step.size()-enz)*1.0*sEntry.getValue() / (l1*(step.size()-1)) + (enz-1)*1.0/(step.size()-1);
	    p=Math.min(1.0,p);
	    if (rand.nextDouble() <= p)
		ret.put(sEntry.getKey(), sEntry.getValue() / p);
	}
	return ret;
    }

    public Map<Integer, Double> walkTermTerm(Map<Integer,Double> x, int dtNeighbors, int tdNeighbors, int dEnz, int tEnz)
    {
	return walkOneWay(walkOneWay(x, tdNeighbors, dEnz, TermDocB), dtNeighbors, tEnz, DocTermB);
    }

    public void l1Normalize(Map<Integer, Double> x) {
	double l1 = l1Norm(x);
	for (Map.Entry<Integer, Double> e : x.entrySet()) e.setValue(e.getValue() / l1);
    }

    public void train(){
	COSample = new Map[numTerms];
	int vecNum = 0;
	for (int term = 0; term < numTerms; ++term) {
	    Map<Integer, Double> startTerm = new HashMap<Integer, Double>();
	    startTerm.put(term, 1.0);
	    COSample[term] = walkTermTerm(startTerm, this.dtNs, this.tdNs, docEnzs, termEnzs);
	}
    }

    protected void sparseAddTo(Map<Integer, Double> dest, double mult, Map<Integer, Double> add) {
	for (Map.Entry<Integer, Double> e : add.entrySet()) {
	    addEntry(dest, e.getKey(), mult * e.getValue());
	}
    }
    
    public Integer[] predict(int k, PredictionPaper testPaper) {
	PriorityQueue<ItemAndScore> queue = 
	    new PriorityQueue<ItemAndScore>(k+1);

	Map<Integer,Double> COscore=new HashMap<Integer,Double>();
	for (Integer id : testPaper.getTrainingWords()) {
	    double wt=(TermDocB[id].size()==0)?0:testPaper.getTrainingTf(id)*1.0/TermDocB[id].size();
	    sparseAddTo(COscore,wt,COSample[id]);
	}
	
	for (Map.Entry<Integer,Double> e:COscore.entrySet()){
	    int id = e.getKey();
	    double idf=terms.get(id).trainingIdf(trainingSet.size());
	    double score = e.getValue()*idf;
	    
	    if (testPaper.getTrainingTf(id)>0)
		continue;
	    if (queue.size() < k || score > queue.peek().score) {	    
		if (queue.size() >= k)
		    queue.poll();
		queue.add(new ItemAndScore(new Integer(id), score, true));
	    }
	}

	Integer[] results = new Integer[Math.min(k, queue.size())];
	for (int i = 0; i < k && !queue.isEmpty(); i ++) {
	    results[i] = (Integer)queue.poll().item;
	}

	return results;
    }

    // protected Map<Integer, Double> represent(PaperIF doc) {
    // 	Map<Integer, Double> ret = new HashMap<Integer, Double>();
    // 	for (Integer w : doc.getTrainingWords()) {
    // 	    double tf = doc.getTrainingTf(w);
    // 	    sparseAddTo(ret, tf, localVectorsT[w]);
    // 	}
    // 	return ret;
    // }

    // /** Computes the similarity between documents a and b.  a must be a training document. */
    // public double similarity(int aIndex, PaperIF b) {
    // 	Map<Integer, Double> aRepr = trainingRepresentations.get(aIndex);
    // 	Map<Integer, Double> bRepr = represent(b);
    // 	return sparseDot(aRepr, bRepr);
    // }
}
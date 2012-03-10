package plusone.utils;

import plusone.Main;
import plusone.utils.TrainingPaper;
import plusone.utils.PredictionPaper;
import plusone.utils.Plot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

public class LocalSVDish {

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
    protected Map<Integer, Double>[] DocTermB, TermDocB;
    protected Map<Integer, Double>[] localVectorsT;  // The result of training.
    protected Map<Integer, Map<Integer, Double>> trainingRepresentations;
    public int numTerms;
    private Random rand=new Random();
    private Plot plot;
    private List<Double>[][] plotData;

    int nLevels;
    int[] docEnzs;
    int[] termEnzs;
    int[] dtNs;
    int[] tdNs;
    int[] numLVecs;
    int walkLength;

    public LocalSVDish(int nLevels, int[] docEnzs, int[] termEnzs, int[] dtNs, int[] tdNs,
		       int[] numLVecs,
		       List<TrainingPaper> trainingSet, int num, int walkLength) {

	this.nLevels = nLevels;
	this.docEnzs = docEnzs;
	this.termEnzs = termEnzs;
	this.dtNs = dtNs;
	this.tdNs = tdNs;
	this.numLVecs = numLVecs;
	this.walkLength=walkLength;
	this.plot = new Plot("localSVD.html");
	this.trainingSet = trainingSet;
	numTerms = num;
	plotData = new List[nLevels][walkLength+1];
	for (int j=0;j<nLevels;++j) {
	    for (int i=0;i<walkLength+1;i++){
		plotData[j][i]=new LinkedList<Double>();
	    }
	}

	long t1 = System.currentTimeMillis();
	System.out.println("[SVDLocalSVDish] training");

	DocTermB = new Map[trainingSet.size()];
        for (int i = 0; i < DocTermB.length; ++i) DocTermB[i] = new HashMap<Integer, Double>();
	TermDocB = new Map[numTerms];
        for (int i = 0; i < TermDocB.length; ++i) TermDocB[i] = new HashMap<Integer, Double>();
	for (int i = 0; i < trainingSet.size(); i ++) {
	    TrainingPaper doc = trainingSet.get(i);

	    for (Integer word : doc.getTrainingWords()) {
		Entry temp = new Entry(i, word, doc.getTrainingTf(word));
		addEntry(DocTermB[i], word, doc.getTrainingTf(word));
		addEntry(TermDocB[word], i, doc.getTrainingTf(word));
	    }
	}

	this.train();
	System.out.format("[LocalSVDish]] took %.3f seconds.\n",
			  (System.currentTimeMillis() - t1)/1000.0);
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
    public Map<Integer, Double> oneFullStep(Map<Integer,Double>[] A, Map<Integer,Double> x){
	Map<Integer,Double> ret = new HashMap<Integer,Double>();
	for (Map.Entry<Integer, Double> xEntry : x.entrySet())
	    {
		Map<Integer, Double> connected = A[xEntry.getKey()];
		for (Map.Entry<Integer, Double> cEntry : connected.entrySet()) {
		    addEntry(ret, cEntry.getKey(), cEntry.getValue() * xEntry.getValue()/connected.size());
		}
	    }
	return ret;
    }

    public Map<Integer, Double> walkOneWay(Map<Integer,Double> x, int neighbors, int enz, Map<Integer, Double>[] A)
    {
	Map<Integer, Double> step = new HashMap<Integer, Double>();
	for (Map.Entry<Integer, Double> xEntry : x.entrySet())
	    {
		Map<Integer, Double> connected = A[xEntry.getKey()];
		for (Map.Entry<Integer, Double> cEntry : connected.entrySet()) {
		    // Randomly include each neighbor with probability (neighbors/ # actual neighbors)
		    // This should be an unbiased estimator for (row xEntry.key() of A) * xEntry.value() * neighbors / (# actual neighbors).
		    if (rand.nextDouble() < neighbors * 1.0 / connected.size())
			addEntry(step, cEntry.getKey(), cEntry.getValue() * xEntry.getValue() / Math.min(neighbors, connected.size()));
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
    public double spreadiness(Map<Integer,Double> vec){
	Map<Integer,Double> nextVec = oneFullStep(DocTermB,oneFullStep(TermDocB,vec));
	return sparseDot(nextVec,vec);
    }
    public void train(){
	localVectorsT = new Map[numTerms];
	plot.beginReport();
        for (int i = 0; i < localVectorsT.length; ++i) localVectorsT[i] = new HashMap<Integer, Double>();
	int vecNum = 0;
	for (int level = 0; level < nLevels; ++level) {
	    int docEnz = docEnzs[level];
	    int termEnz = termEnzs[level];
	    int dtNeighbors = dtNs[level];
	    int tdNeighbors = tdNs[level];
	    for (int i = 0; i < numLVecs[level]; ++i) {
		//start with a random term.
		
		Map<Integer, Double> startTerm = new HashMap<Integer, Double>();
		startTerm.put(rand.nextInt(numTerms), 1.0);
		Map<Integer,Double> localVec=startTerm;
		plotData[level][0].add(spreadiness(localVec));
		for (int j=0;j<walkLength;j++){
		    localVec = walkTermTerm(localVec, dtNeighbors, tdNeighbors, docEnz, termEnz);
		    plotData[level][j+1].add(spreadiness(localVec));
		l1Normalize(localVec);
		}
		for (Map.Entry<Integer, Double> e : localVec.entrySet()) {
		    localVectorsT[e.getKey()].put(vecNum, e.getValue());
		}
		++ vecNum;
	    }
	    for (int i=0;i<walkLength+1;i++){
		plot.report("Level: "+ level+ " walkLength: "+i);
		plot.reportHistogram(plotData[level][i],100);
	    }
	}
	plot.endReport();

	trainingRepresentations = new HashMap<Integer, Map<Integer, Double>>();
       
	for (int i = 0; i < trainingSet.size(); ++i) {
            TrainingPaper tr = trainingSet.get(i);
	    trainingRepresentations.put(tr.getIndex(), represent(tr));
	}
    }

    protected void sparseAddTo(Map<Integer, Double> dest, double mult, Map<Integer, Double> add) {
	for (Map.Entry<Integer, Double> e : add.entrySet()) {
	    addEntry(dest, e.getKey(), mult * e.getValue());
	}
    }

    protected Map<Integer, Double> represent(PaperIF doc) {
	Map<Integer, Double> ret = new HashMap<Integer, Double>();
	for (Integer w : doc.getTrainingWords()) {
	    double tf = doc.getTrainingTf(w);
	    sparseAddTo(ret, tf, localVectorsT[w]);
	}
	return ret;
    }

    /** Computes the similarity between documents a and b.  a must be a training document. */
    public double similarity(int aIndex, PaperIF b) {
	Map<Integer, Double> aRepr = trainingRepresentations.get(aIndex);
	Map<Integer, Double> bRepr = represent(b);
	return sparseDot(aRepr, bRepr);
    }
}
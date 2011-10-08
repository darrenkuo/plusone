package plusone.utils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import plusone.utils.Term;

public class PaperAbstract {
    public int index, indexInGlobalList;
    public Integer[] abstractText;
    public int[] inReferences;
    public int[] outReferences;
//    public List<Integer> inferenceWords;
    public List<Integer> predictionWords;
    public List<Integer> outputWords;
    boolean test;
    //public int[][] tf; 	//tf[i][0]=occurance of word i in training part, tf[i][1]= total occurance
    public HashMap<Integer, Integer> trainingTf;
    public HashMap<Integer, Integer> testingTf;
    private double length;

    public int uniqueWords; // # of unique words in training text
    public List<Integer> wordSet;
    
    public PaperAbstract(int index, int[] inReferences, 
			 int[] outReferences, String abstractText,
			 Indexer<String> wordIndexer,
			 int indexInGlobalList) {
	this.index = index;
	this.indexInGlobalList = indexInGlobalList;
	
	String[] words = abstractText.trim().split(" ");   
	this.abstractText = new Integer[words.length];
	uniqueWords=0;
	wordSet=new ArrayList<Integer>();
	length=0;
	for (int i = 0; i < words.length; i ++) {
	    this.abstractText[i] = wordIndexer.fastAddAndGetIndex(words[i]);
	}

	this.inReferences = inReferences;
	if (this.inReferences == null) 
	    this.inReferences = new int[0];

	this.outReferences = outReferences;
	if (this.outReferences == null)
	    this.outReferences = new int[0];
	
	//if (percentUsed < 1.0)
	//this.generateTestset(percentUsed, wordIndexer);
    }

/*    public void generateTestset(double percentUsed, 
				Indexer<String> wordIndexer) {
	this.inferenceWords = new ArrayList<Integer>();
	this.predictionWords = new ArrayList<Integer>();

	Random random = new Random();
	for (Integer wordID : this.abstractText) {
	    if (percentUsed == 1.0 || random.nextDouble() < percentUsed)
		this.inferenceWords.add(wordID);
	    else
		this.predictionWords.add(wordID);
	}

	this.outputWords = this.inferenceWords;
    } */
    
    public void generateData(double percentUsed,Indexer<String>wordIndexer, Term[] terms, boolean test){
    	this.outputWords = new ArrayList<Integer>();
    	this.predictionWords = new ArrayList<Integer>();
    	this.test=test;
    	//this.tf = new int[wordIndexer.size()][2];
	this.trainingTf = new HashMap<Integer, Integer>();
	if (test)
	    this.testingTf = new HashMap<Integer, Integer>();
    	
    	Random random = new Random();
    	for (Integer wordID : this.abstractText) {
    	    if ((!test) || random.nextDouble() > percentUsed){
    		this.outputWords.add(wordID);
    		if (!this.trainingTf.containsKey(wordID))
    		{
		    /* FIXME: Adding testing documents to a field called
		     * doc_train (which Term.addDoc does) seems to be asking
		     * for trouble.  I have almost written code that trained on
		     * testing documents because of this.  -James */
		    terms[wordID].addDoc(this, false);
		    uniqueWords++;
		    wordSet.add(wordID);
		    this.trainingTf.put(wordID, 0);
    		}
		this.trainingTf.put(wordID, this.trainingTf.get(wordID) + 1);
    		//tf[wordID][0]++;
    		terms[wordID].totalCount++;
    	    }
    	    else{
    		this.predictionWords.add(wordID);
    		//if (tf[wordID][0]==tf[wordID][1])
		if (!testingTf.containsKey(wordID)) {
		    terms[wordID].addDoc(this,true);
		    this.testingTf.put(wordID, 0);
		}

		this.testingTf.put(wordID, this.testingTf.get(wordID) + 1);
    	    }
    	    //tf[wordID][1]++;
    	}
    	
    	double length=0.0;
    	
    	Set<Map.Entry<Integer, Integer>> words = this.trainingTf.entrySet();
    	
    	Iterator<Map.Entry<Integer,Integer>> iterator= words.iterator();
    	
    	while (iterator.hasNext()){
    		Map.Entry<Integer, Integer> entry = iterator.next();
    		int count=entry.getValue();
    		length+=count*count;
    	}
    	this.length = Math.sqrt(length);

//    	this.outputWords = this.inferenceWords;
    	
    }
    public String toString() {
	String results = "";
	results += "INDEX " + this.index + "\n";
	results += "IN REF " + Arrays.toString(this.inReferences) + "\n";
	results += "OUT REF " + Arrays.toString(this.outReferences) + "\n";
	results += "ABSTRACT " + this.abstractText + "\n";

	return results;
    }

    public Integer getTf0(int wordID) {
	return this.trainingTf.containsKey(wordID) ? this.trainingTf.get(wordID) : 0;
    }

    public Integer getTf1(int wordID) {
	Integer tf0 = getTf0(wordID);
	if (!this.test) {
	    return tf0;
	}
	return tf0 + (this.testingTf.containsKey(wordID) ? this.testingTf.get(wordID) : 0);
    }
    
    public double getLength(){
    	return length;
    }
    
    public double similarity(PaperAbstract a){
    	double dist = 0.0;
    	
    	Set<Map.Entry<Integer, Integer>> words = this.trainingTf.entrySet();
    	
    	Iterator<Map.Entry<Integer,Integer>> iterator= words.iterator();
    	
    	while (iterator.hasNext()){
    		Map.Entry<Integer, Integer> entry = iterator.next();
    		int wordId=entry.getKey();
    		int count=entry.getValue();
    		if (a.trainingTf.containsKey(wordId))
    			dist+=count*a.trainingTf.get(wordId);
    		
    	}
    	return dist;
    }

    public boolean equals(Object obj) {
	if (obj instanceof PaperAbstract)
	    return this.index == ((PaperAbstract)obj).index;
	return false;
    }

    public SparseVec makeTrainingWordVec(boolean useFreqs, int nDocs, Term[] terms) {
        SparseVec ret = new SparseVec();
        for (Map.Entry<Integer, Integer> entry : trainingTf.entrySet())
            ret.addSingle(entry.getKey(), (useFreqs ? entry.getValue() : 1.0) * terms[entry.getKey()].trainingIdf(nDocs));
        return ret;
    }
}

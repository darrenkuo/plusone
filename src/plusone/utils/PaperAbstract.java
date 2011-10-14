package plusone.utils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import plusone.utils.Term;

public class PaperAbstract {
    public int index, indexInGlobalList;
    public Integer[] abstractText;
    public int[] inReferences;
    public int[] outReferences;

    /*
     * contains all the words for training or predicting
     * training documents will have all it's words in here
     */    
    public List<Integer> modelWords;
    /*
     * contains all the words for testing answer checking
     * testing documents will have some words in here
     */
    public List<Integer> answerWords;
    public HashMap<Integer, Integer> trainingTf;
    public HashMap<Integer, Integer> testingTf;
    public boolean test = false;
    public int uniqueWords; // # of unique words in training text
    public double norm;
    public Indexer<String> wordIndexer;

    public PaperAbstract(int index, int[] inReferences, 
			 int[] outReferences, String abstractText,
			 Indexer<String> wordIndexer,
			 int indexInGlobalList) {
	this.index = index;
	this.indexInGlobalList = indexInGlobalList;
	
	String[] words = abstractText.trim().split(" ");   
	this.abstractText = new Integer[words.length];
	uniqueWords = 0;

	for (int i = 0; i < words.length; i ++) {
	    this.abstractText[i] = wordIndexer.fastAddAndGetIndex(words[i]);
	}

	this.inReferences = inReferences;
	if (this.inReferences == null) 
	    this.inReferences = new int[0];

	this.outReferences = outReferences;
	if (this.outReferences == null)
	    this.outReferences = new int[0];
	
	this.wordIndexer = wordIndexer;
    }

    public void generateData(double percentUsed,
			     Term[] terms, boolean test){
	this.test = test;
    	answerWords = new ArrayList<Integer>();
    	modelWords = new ArrayList<Integer>();
	trainingTf = new HashMap<Integer, Integer>();
	if (test)
	    testingTf = new HashMap<Integer, Integer>();

	HashMap<Integer, Integer> tf = new HashMap<Integer, Integer>();
	for (Integer wordID : abstractText) {
	    if (!tf.containsKey(wordID))
		tf.put(wordID, 0);
	    tf.put(wordID, tf.get(wordID) + 1);
	}
	uniqueWords = tf.keySet().size();

	Iterator<Integer> iter = tf.keySet().iterator();
	int c = 0;
	while (iter.hasNext()) {
	    Integer word = iter.next();
	    if (terms != null)
		terms[word].addDoc(this, test);
	    if (test && c < percentUsed * tf.keySet().size()) {
		answerWords.add(word);
		this.testingTf.put(word, tf.get(word));
	    } else {
		modelWords.add(word);
		this.trainingTf.put(word, tf.get(word));
		if (!test && terms != null)
		    terms[word].totalCount += tf.get(word);
	    }
	    c ++;
	}

    	Set<Map.Entry<Integer, Integer>> words = trainingTf.entrySet();
    	Iterator<Map.Entry<Integer,Integer>> iterator = words.iterator();
    	
    	while (iterator.hasNext()){
	    Map.Entry<Integer, Integer> entry = iterator.next();
	    int count = entry.getValue();
	    norm += count * count;
    	}
    	norm = Math.sqrt(norm);
    }

    public String toString() {
	String results = "";
	results += "INDEX " + index + "\n";
	results += "IN REF " + Arrays.toString(inReferences) + "\n";
	results += "OUT REF " + Arrays.toString(outReferences) + "\n";
	results += "ABSTRACT " + abstractText + "\n";

	return results;
    }

    public Integer getModelTf(int wordID) {
	return trainingTf.containsKey(wordID) ? 
	    trainingTf.get(wordID) : 0;
    }

    public Integer getTf(int wordID) {
	Integer tf0 = getModelTf(wordID);
	if (!test) {
	    return tf0;
	}
	return tf0 + (testingTf.containsKey(wordID) ? 
		      testingTf.get(wordID) : 0);
    }
    
    public double getNorm(){
    	return norm;
    }
    
    public double similarity(PaperAbstract a){
    	double dist = 0.0;
    	
    	Set<Map.Entry<Integer, Integer>> words = trainingTf.entrySet();
    	Iterator<Map.Entry<Integer,Integer>> iterator= words.iterator();

    	while (iterator.hasNext()){
    		Map.Entry<Integer, Integer> entry = iterator.next();
    		int wordId = entry.getKey();
    		int count = entry.getValue();
    		if (a.trainingTf.containsKey(wordId)) {
		    dist += count * a.trainingTf.get(wordId);
		}
    	}
    	return dist/(a.norm * norm);
    }

    public boolean equals(Object obj) {
	if (obj instanceof PaperAbstract)
	    return this.index == ((PaperAbstract)obj).index;
	return false;
    }

    public SparseVec makeTrainingWordVec(boolean useFreqs, 
					 int nDocs, Term[] terms) {
        SparseVec ret = new SparseVec();
        for (Map.Entry<Integer, Integer> entry : trainingTf.entrySet())
            ret.addSingle(entry.getKey(), 
			  (useFreqs ? entry.getValue() : 1.0) * 
			  terms[entry.getKey()].trainingIdf(nDocs));
        return ret;
    }
}

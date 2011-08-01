package plusone.utils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import plusone.utils.Term;

public class PaperAbstract {
    public int index;
    public Integer[] abstractText;
    public int[] inReferences;
    public int[] outReferences;
//    public List<Integer> inferenceWords;
    public List<Integer> predictionWords;
    public List<Integer> outputWords;
    boolean test;
    public int[][] tf; 	//tf[i][0]=occurance of word i in training part, tf[i][1]= total occurance
    public int uniqueWords; // # of unique words in training text
    public List<Integer> wordSet;
    
    public PaperAbstract(int index, int[] inReferences, 
			 int[] outReferences, String abstractText,
			 Indexer<String> wordIndexer) {
	this.index = index;
	
	String[] words = abstractText.trim().split(" ");   
	this.abstractText = new Integer[words.length];
	uniqueWords=0;
	wordSet=new ArrayList<Integer>();
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
    	tf=new int[wordIndexer.size()][2];
    	
    	Random random = new Random();
    	for (Integer wordID : this.abstractText) {
    	    if ((!test) || random.nextDouble() < percentUsed){
    		this.outputWords.add(wordID);
    		if (tf[wordID][0]==0)
    		{
    			terms[wordID].addDoc(this, false);
    			uniqueWords++;
    			wordSet.add(wordID);
    		}
    		tf[wordID][0]++;
    		terms[wordID].totalCount++;
    	    }
    	    else{
    		this.predictionWords.add(wordID);
    		if (tf[wordID][0]==tf[wordID][1])
    			terms[wordID].addDoc(this,true);
    	    }
    	    tf[wordID][1]++;
    	}

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

    
}

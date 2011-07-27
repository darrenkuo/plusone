package plusone.utils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PaperAbstract {
    public int index;
    public Integer[] abstractText;
    public int[] inReferences;
    public int[] outReferences;
    public List<Integer> inferenceWords;
    public List<Integer> predictionWords;
    public List<Integer> outputWords;
    
    public PaperAbstract(int index, int[] inReferences, 
			 int[] outReferences, String abstractText,
			 double percentUsed, Indexer<String> wordIndexer) {
	this.index = index;
	
	String[] words = abstractText.trim().split(" ");   
	this.abstractText = new Integer[words.length];
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
	this.generateTestset(percentUsed, wordIndexer);
    }

    public void generateTestset(double percentUsed, 
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

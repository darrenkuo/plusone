package plusone.utils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PaperAbstract {
    public int index;
    public String[] abstractText;
    public int[] inReferences;
    public int[] outReferences;
    public List<String> inferenceWords;
    public List<String> predictionWords;
    public List<String> outputWords;
    
    public PaperAbstract(int index, int[] inReferences, 
			 int[] outReferences, String abstractText,
			 double percentUsed) {
	this.index = index;
	this.abstractText = abstractText.trim().split(" ");

	this.inReferences = inReferences;
	if (this.inReferences == null) 
	    this.inReferences = new int[0];

	this.outReferences = outReferences;
	if (this.outReferences == null)
	    this.outReferences = new int[0];

	this.outputWords = Arrays.asList(this.abstractText);
	if (percentUsed < 1.0) {
	    generateTestset(percentUsed);
	}
    }

    public void generateTestset(double percentUsed) {
	this.inferenceWords = new ArrayList<String>();
	this.predictionWords = new ArrayList<String>();

	Random random = new Random();
	for (String word : this.outputWords) {
	    if (percentUsed == 1.0 || random.nextDouble() < percentUsed)
		this.inferenceWords.add(word);
	    else
		this.predictionWords.add(word);
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

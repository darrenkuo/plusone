package plusone.utils;

import java.util.Arrays;
import java.util.List;

public class TFIDFCounter {

    public static double LOWEST_TFIDF = 2.0;

    public WordAndScore[] wordFrequency;
    private int[][] tf;
    private double[] idf;
    private List<PaperAbstract> abstracts;
    private Indexer<String> wordIndexer;

    /*
     * tf[document][term] = number of occurence of term in the given
     * document 
     * idf[term] = log({number of documents}/{number of
     * documents that contains that term})
     */
    public TFIDFCounter(List<PaperAbstract> abstracts, 
			Indexer wordIndexer) {
	this.abstracts = abstracts;
	this.wordIndexer = wordIndexer;

	this.wordFrequency = new WordAndScore[wordIndexer.size()];
	this.tf = new int[abstracts.size()][];
	this.idf = new double[wordIndexer.size()];

	System.out.println("calculating TFIDF");
	calculateTFIDF();
    }

    private void calculateTFIDF() {
	double idf_top =  Math.log((double)this.abstracts.size());

	for (int w = 0; w < this.wordIndexer.size(); w ++) {
	    this.wordFrequency[w] = new WordAndScore(w, 0, false);
	}

	for (int i = 0; i < this.abstracts.size(); i ++) {
	    PaperAbstract a = this.abstracts.get(i);
	    this.tf[i] = new int[wordIndexer.size()];
	    for (Integer wordID : a.abstractText) {
		this.tf[i][wordID] += 1;
		this.wordFrequency[wordID].score 
		    += 1;
	    }
	    
	    for (int j = 0; j < tf[i].length; j ++) {
		if (this.tf[i][j] > 0) {
		    this.idf[j] += 1;
		}
	    }
	}

	for (int i = 0; i < idf.length; i ++) {
	    if (this.idf[i] == 0) 
		this.idf[i] = LOWEST_TFIDF;
	    else
		this.idf[i] = idf_top - Math.log((double)idf[i]);
	}

	Arrays.sort(this.wordFrequency);

	for (int i = 0; i < 30 && i < this.wordFrequency.length; i ++) {
	    System.out.println("i: " + i + " word: " + 
			       this.wordIndexer.get(this.wordFrequency[i].wordID) + " ---- count: " + this.wordFrequency[i].score);
	}

    }

    public int tf(int document, int term) {
	return tf[document][term];
    }
    
    public double idf(int term) {
	return idf[term];	
    }

    public double tfidf(int document, int term) {
	return tf(document, term) * idf(term);
    }
}
package plusone.clustering;

import plusone.utils.Indexer;
import plusone.utils.PaperAbstract;
import plusone.utils.Term;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Baseline extends ClusteringTest {
    
    protected List<PaperAbstract> documents;
    protected Indexer<String> wordIndexer;
    protected Term[] terms;
    protected List<PaperAbstract> trainingSet;
    protected List<PaperAbstract> testingSet;

    public Baseline(List<PaperAbstract> documents, 
		    List<PaperAbstract> trainingSet,
		    List<PaperAbstract> testingSet,
		    Indexer<String> wordIndexer,
		    Term[] terms) {
	super("Baseline");
	this.documents = documents;
	this.wordIndexer = wordIndexer;
	this.terms = terms;
	this.trainingSet = trainingSet;
	this.testingSet = testingSet;
    }

    protected int oneMore(List<Integer> topWords){
    	int max = -1, id = -1;
    	for (int i = 0; i < terms.length;i++) {
	    if (!topWords.contains(i) && terms[i].totalCount > max){
		max = terms[i].totalCount;
		id = i;
	    }
	}
	
    	if (id != -1) {
	    topWords.add(id);
    	}
    	return id;
    }
    
    public Integer[][] predict(int k, boolean outputUsedWord, 
			       File outputDirectory) {
	Integer[][] results = new Integer[testingSet.size()][];

	List<Integer> topWords = new ArrayList<Integer>();
	// doesn't this sort the whole list?
	for (int i = 0; i < terms.length && i<k;i++) {
	    oneMore(topWords);
	}

	for (int a = 0; a < testingSet.size(); a ++) {
	    if (outputUsedWord) {
		results[a] = new Integer[Math.min(terms.length, k)];
		for (int w = 0; w < k && w < terms.length; w ++) {
		    results[a][w] = topWords.get(w);
		}
	    } else {
		int c = 0;
		List<Integer> lst = new ArrayList<Integer>();
		for (int w = 0; c < k && w < this.terms.length; w ++) {
		    Integer curWord = (w >= topWords.size() ? 
				       oneMore(topWords) : topWords.get(w));

		    if (testingSet.get(a).getModelTf(curWord) == 0) {
			lst.add(curWord);
			c ++;
		    }
		}
	       
		results[a] = (Integer[])lst.toArray(new Integer[lst.size()]);
	    }
	}
	return results;
    }
}
package plusone.clustering;

import plusone.utils.Indexer;
import plusone.utils.PaperAbstract;
import plusone.utils.PlusoneFileWriter;
import plusone.utils.Term;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Baseline1 extends Baseline {
    
    public Baseline1(List<PaperAbstract> documents, 
		     List<PaperAbstract> trainingSet,
		     List<PaperAbstract> testingSet,
		     Indexer<String> wordIndexer, 
		     Term[] terms) {
	super(documents, trainingSet, testingSet,
	      wordIndexer, terms);
	this.testName = "baseline1";
    }

    public Integer[][] predict(int k, boolean outputUsedWord, File outputDirectory) {
	Integer[][] results = new Integer[testingSet.size()][];

	PlusoneFileWriter writer = null;
	if (outputDirectory != null) {
	    writer = new PlusoneFileWriter(new File(outputDirectory,
						    "Baseline1-" + k + "-" + 
						    outputUsedWord + 
						    ".predict"));
	}
	
	List<Integer> topWords = new ArrayList<Integer>();
	for (int i=0;i<terms.length && i<k;i++){	    
	    oneMore(topWords);
	}

	for (int a = 0; a < testingSet.size(); a ++) {
	    if (outputUsedWord) {
		results[a] = new Integer[Math.min(terms.length, k)];
		for (int w = 0; 
		     w < k && w < terms.length; 
		     w ++) {
		    results[a][w] = topWords.get(w);

		    if (outputDirectory != null)
			writer.write(this.wordIndexer.get(results[a][w]) + " ");
		}
	    } else {
		int c = 0;
		List<Integer> lst = new ArrayList<Integer>();
		for (int w = 0; c < k && w < this.terms.length;
		     w ++) {
		    Integer curWord = -1;
		    if (w >= topWords.size())
			curWord = oneMore(topWords);
		    else
			curWord = topWords.get(w);

		    if (testingSet.get(a).getTf0(curWord) > 0) {
			if (outputDirectory != null)
			    writer.write(this.wordIndexer.get(curWord) + " ");
			lst.add(curWord);
			c ++;
		    }
		}
	       
		results[a] = (Integer[])lst.toArray(new Integer[lst.size()]);
	    }
	    if (outputDirectory != null)
		writer.write("\n");
	}
	
	if (outputDirectory != null)
	    writer.close();
	return results;
    }
}
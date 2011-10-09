package plusone.clustering;

import plusone.utils.Indexer;
import plusone.utils.PaperAbstract;
import plusone.utils.PlusoneFileWriter;
import plusone.utils.Term;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Baseline1 extends Baseline {

    public Term[] sortedTerms;
    private int ti = 0;
    
    public Baseline1(List<PaperAbstract> documents, 
		     List<PaperAbstract> trainingSet,
		     List<PaperAbstract> testingSet,
		     Indexer<String> wordIndexer, 
		     Term[] terms) {
	super(documents, trainingSet, testingSet,
	      wordIndexer, terms);
	this.testName = "baseline1";
	
	this.sortedTerms = new Term[terms.length];

	for (int i = 0; i < terms.length; i ++) {
	    this.sortedTerms[i] = this.terms[i];
	} 

	Arrays.sort(this.sortedTerms, 0, this.sortedTerms.length,
		    new Comparator<Term>() {
			public int compare(Term o1, Term o2) {
			    if (o1.totalCount > o2.totalCount) {
				return -1;
			    }
			    return 1;
			}

			public boolean equals(Object obj) {
			    return false;
			}
		    });
    }

    protected int oneMore() {
	if (ti < this.sortedTerms.length) {
	    return ti ++;
	}
	return -1;
    }

    public void reset() {
	this.ti = 0;
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

	for (int a = 0; a < testingSet.size(); a ++) {
	    if (outputUsedWord) {
		List<Integer> lst = new ArrayList<Integer>();
		for (int c = 0; c < k; c ++) {
		    Integer curWord = oneMore();

		    if (curWord == -1)
			break;

		    if (testingSet.get(a).getModelTf(curWord) > 0) {
			if (outputDirectory != null)
			    writer.write(this.wordIndexer.get(curWord) + " ");
			lst.add(curWord);
			c ++;
		    }
		}
		results[a] = (Integer[])lst.toArray(new Integer[lst.size()]);
	    } else {
		results[a] = new Integer[0];
	    }
	    if (outputDirectory != null)
		writer.write("\n");
	}
	
	if (outputDirectory != null)
	    writer.close();
	return results;
    }
}
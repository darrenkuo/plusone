package plusone.clustering;

import plusone.utils.Indexer;
import plusone.utils.PaperAbstract;
import plusone.utils.Term;

import java.util.ArrayList;
import java.util.List;

public class Baseline extends ClusteringTest {
    
    private List<PaperAbstract> documents;
    private Indexer<String> wordIndexer;
    private Term[] terms;
    private List<PaperAbstract> trainingSet;
    private List<PaperAbstract> testingSet;

    public Baseline(List<PaperAbstract> documents, List<PaperAbstract> trainingSet,
    				List<PaperAbstract> testingSet,Indexer<String> wordIndexer, Term[] terms) {
	super("Baseline");
	this.documents = documents;
	this.wordIndexer = wordIndexer;
	this.terms = terms;
	this.trainingSet=trainingSet;
	this.testingSet=testingSet;
    }

    @Override
    public void analysis(int numPred, boolean usedWord) {
	super.analysis(numPred, usedWord);

//	this.baselineTest(testingSet, numPred, usedWord);
    }

/*    private void baselineTest(List<PaperAbstract> abstracts, 
			      int k, boolean usedWord) {
	// need global word count
	// somehow get top K words from the global word count

	Integer[][] predictedWords = predictTopKWordsNaive(abstracts, k, 
							   usedWord);
	
	int predicted = 0, total = 0;
	double tfidfScore = 0.0, idfScore = 0.0;
	double idf_top =  Math.log((double)this.documents.size());
	for (int i = 0; i < abstracts.size(); i++) {
		PaperAbstract doc=abstracts.get(i);
	    for (int j = 0; j < predictedWords[i].length; j++) {
		Integer wordID = predictedWords[i][j];
		if (doc.predictionWords.contains(wordID)) {
		    predicted ++;
		    double temp=(idf_top-Math.log((double)(terms[wordID].idfRaw()+(doc.outputWords.contains(wordID)?0:1)))); 
		    tfidfScore += doc.tf[wordID][1]*temp;
		    idfScore += temp;
		}
		total ++;
	    }
	} 
	System.out.println("Predicted " + ((double)predicted/total) * 100 + 
			   " percent of the words");
	System.out.println("TFIDF score: " + tfidfScore);
	System.out.println("IDF score: " + idfScore);
    } */
    
    
    private int oneMore(List<Integer> topWords){
    	int max=-1;
    	int id = -1;
    	for (int i=0;i<terms.length;i++)
    		if (!topWords.contains(i) && terms[i].totalCount>max){
    			max=terms[i].totalCount;
    			id =i;
    		}
    	if (id!=-1){
    	topWords.add(id);
    	}
    	return id;
    }
    
    public Integer[][] predict(int k, boolean outputUsedWord) {
	Integer[][] results = new Integer[testingSet.size()][];
	
	List<Integer> topWords = new ArrayList<Integer>();
	for (int i=0;i<terms.length && i<k;i++){
		oneMore(topWords); 
	}
	for (int a = 0; a < testingSet.size(); a ++) {
	    if (outputUsedWord) {
		results[a] = 
		    new Integer[Math.min(terms.length, k)];
		for (int w = 0; 
		     w < k && w < terms.length; 
		     w ++) {
		    results[a][w] = topWords.get(w);
		}
	    } else {
		int c = 0;
		List<Integer> lst = new ArrayList<Integer>();
		for (int w = 0; c < k && w < this.terms.length;
		     w ++) {
			Integer curWord=-1;
			if (w>=topWords.size())
				curWord=oneMore(topWords);
			else
				curWord = topWords.get(w);
		    if (testingSet.get(a).tf[curWord][0]==0) {
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
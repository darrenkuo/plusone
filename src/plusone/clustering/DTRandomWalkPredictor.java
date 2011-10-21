package plusone.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import plusone.utils.PredictionPaper;
import plusone.utils.SparseVec;
import plusone.utils.Term;
import plusone.utils.TrainingPaper;

/** Does a random walk on the document-topic graph to find words.
 */
public class DTRandomWalkPredictor extends ClusteringTest {
    protected List<TrainingPaper> trainingSet;
    protected Term[] terms;
    protected int walkLength;
    protected List<Integer[]> predictions;
    protected Map<Integer, SparseVec> docsForEachWord;

    public DTRandomWalkPredictor(List<TrainingPaper> trainingSet,
                                 Term[] terms,
                                 int walkLength) {
        super("DTRandomWalkPredictor-" + Integer.toString(walkLength));
        this.trainingSet = trainingSet;
        this.terms = terms;
        this.walkLength = walkLength;
        this.docsForEachWord = makeDocsForEachWord(trainingSet);
    }
    
    protected Map<Integer, SparseVec> makeDocsForEachWord(List<TrainingPaper> trainingSet) {
	Map<Integer, SparseVec> ret = new HashMap<Integer, SparseVec>();
	for (int i = 0; i < trainingSet.size(); ++i) {
	    TrainingPaper paper = trainingSet.get(i);
	    for (Integer word : paper.getTrainingWords()) {
		if (!ret.containsKey(word)) ret.put(word, new SparseVec());
		ret.get(word).addSingle(i, (double)paper.getTrainingTf(word));
	    }
	}
	return ret;
    }

    public Integer[] predict(int k, PredictionPaper paper) {
	SparseVec words = detWalk(paper);
	return firstKExcluding(words.descending(), k, paper.getTrainingWords());
    }

    Integer[] firstKExcluding(Integer[] l, Integer k, Set<Integer> excl) {
	List<Integer> ret = new ArrayList<Integer>();
	for (int i = 0; i < l.length && ret.size() < k; ++i) {
	    Integer word = l[i];
	    if (excl == null || !excl.contains(word)) {
		ret.add(word);
	    }
	}
	return ret.toArray(new Integer[0]);
    }

    protected SparseVec detWalk(PredictionPaper start) {
	SparseVec words = new SparseVec(start);
        int nDocs = trainingSet.size();
	for (int i = 0; i < walkLength; ++i) {
	    /* Walk from words to docs. */
	    SparseVec docs = new SparseVec();
	    for (Map.Entry<Integer, Double> pair : words.pairs()) {
                Term term = terms[pair.getKey()];
                SparseVec docsForThisWord = docsForEachWord.get(pair.getKey());
                if (null != docsForThisWord)
                    docs.plusEqualsWithCoef(docsForThisWord, pair.getValue() / term.totalCount);
	    }
	    /* Walk from docs to words. */
	    words = new SparseVec();
	    for (Map.Entry<Integer, Double> pair : docs.pairs()) {
		SparseVec wordsForThisDoc = makeTrainingWordVec(trainingSet.get(pair.getKey()), true, nDocs, terms);
		wordsForThisDoc.dotEquals(pair.getValue() / wordsForThisDoc.coordSum());
		words.plusEquals(wordsForThisDoc);
	    }
	}
	return words;
    }

    public SparseVec makeTrainingWordVec(
	    TrainingPaper paper, boolean useFreqs,
	    int nDocs, Term[] terms) {
        SparseVec ret = new SparseVec();
        for (Integer word: paper.getTrainingWords())
            ret.addSingle(word, 
			  (useFreqs ? paper.getTrainingTf(word) : 1.0));
        return ret;
    }
}

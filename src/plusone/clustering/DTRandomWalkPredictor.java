package plusone.clustering;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;
import plusone.utils.Document;
import plusone.utils.Indexer;
import plusone.utils.PaperAbstract;
import plusone.utils.PlusoneFileWriter;
import plusone.utils.SparseIntIntVec;
import plusone.utils.Term;

/** Does a random walk on the document-topic graph to find words.
 */
public class DTRandomWalkPredictor extends ClusteringTest {
    protected List<PaperAbstract> documents;
    protected List<PaperAbstract> trainingSet;
    protected List<PaperAbstract> testingSet;
    protected Indexer<String> wordIndexer;
    protected Term[] terms;
    protected int walkLength;
    protected boolean stochastic;
    protected int nSampleWalks;
    protected static Random rand = new Random();

    public DTRandomWalkPredictor(List<PaperAbstract> documents,
                                 List<PaperAbstract> trainingSet,
                                 List<PaperAbstract> testingSet,
                                 Indexer<String> wordIndexer,
                                 Term[] terms,
                                 int walkLength,  
				 boolean stochastic,
                                 int nSampleWalks) {
        super("DTRandomWalkPredictor");
        this.documents = documents;
        this.trainingSet = trainingSet;
        this.testingSet = testingSet;
        this.wordIndexer = wordIndexer;
        this.terms = terms;
        this.walkLength = walkLength;
        this.stochastic = stochastic;
        this.nSampleWalks = nSampleWalks;
    }

    public DTRandomWalkPredictor(List<PaperAbstract> documents,
                                 List<PaperAbstract> trainingSet,
                                 List<PaperAbstract> testingSet,
                                 Indexer<String> wordIndexer,
                                 Term[] terms,
				 int walkLength) {
	this(documents, trainingSet, testingSet, wordIndexer, terms, walkLength, false, -1);
    }

    public DTRandomWalkPredictor(List<PaperAbstract> documents,
                                 List<PaperAbstract> trainingSet,
                                 List<PaperAbstract> testingSet,
                                 Indexer<String> wordIndexer,
                                 Term[] terms,
				 int walkLength,
				 int nSampleWalks) {
	this(documents, trainingSet, testingSet, wordIndexer, terms, walkLength, true, nSampleWalks);
    }

    public Integer[][] predict(int k, boolean outputUsedWord, File outputDirectory) {
	PlusoneFileWriter writer =
	    makePredictionWriter(k, outputUsedWord, outputDirectory,
				 stochastic ? Integer.toString(walkLength) + "-" + Integer.toString(nSampleWalks)
				            : "det");
        Integer[][] ret = new Integer[testingSet.size()][];
	for (int document = 0; document < testingSet.size(); 
	     document ++) {
	    PaperAbstract a = testingSet.get(document);

            SparseIntIntVec words;
	    if (stochastic) {
		/* Add together words at the end of nSampleWalks random walks. */
		words = new SparseIntIntVec();
		for (int i = 0; i < nSampleWalks; ++ i) {
		    PaperAbstract endOfWalk = stochWalk(a);
		    if (null == endOfWalk) continue;
		    words.plusEquals(new SparseIntIntVec(endOfWalk));
		}
	    } else {
		words = detWalk(a);
	    }

            ret[document] = words.topKExcluding(k, outputUsedWord ? null : a);
	    for (int i = 0; i < ret[document].length; ++ i) {
                writer.write(wordIndexer.get(ret[document][i]) + " " );
	    }
            writer.write("\n");
	}

        writer.close();
	return ret;
    }

    protected PaperAbstract stochWalk(PaperAbstract start) {
        PaperAbstract abs = start;
        for (int i = 0; i < walkLength; ++i) {
            List<Integer> words = abs.outputWords;
            if (words.size() == 0) {
                if (i != 0) throw new Error("assertion failed");
                return null;
            }
            Integer word = words.get(rand.nextInt(words.size()));
            List<PaperAbstract> wordDocs = terms[word].getDocTrain();
            if (wordDocs.size() == 0) {
                if (i != 0) throw new Error("assertion failed");
                return null;
            }
            abs = wordDocs.get(rand.nextInt(wordDocs.size()));
        }
        return abs;
    }

    protected SparseIntIntVec detWalk(PaperAbstract start) {
	SparseIntIntVec words = new SparseIntIntVec(start);
	for (int i = 0; i < walkLength; ++i) {
	    /* Walk from words to docs. */
	    SparseIntIntVec docs = new SparseIntIntVec();
	    for (Map.Entry<Integer, Integer> pair : words.pairs()) {
		SparseIntIntVec docsForThisWord = terms[pair.getKey()].makeTrainingDocVec();
		docsForThisWord.dotEquals(pair.getValue());
		docs.plusEquals(docsForThisWord);
	    }
	    /* Walk from docs to words. */
	    words = new SparseIntIntVec();
	    for (Map.Entry<Integer, Integer> pair : docs.pairs()) {
		SparseIntIntVec wordsForThisDoc = new SparseIntIntVec(trainingSet.get(pair.getKey()));
		wordsForThisDoc.dotEquals(pair.getValue());
		words.plusEquals(wordsForThisDoc);
	    }
	}
	return words;
    }
}

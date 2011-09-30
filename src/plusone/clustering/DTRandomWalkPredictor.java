package plusone.clustering;

import java.io.File;
import java.util.List;
import java.util.Random;
import plusone.utils.Document;
import plusone.utils.Indexer;
import plusone.utils.PaperAbstract;
import plusone.utils.PlusoneFileWriter;
import plusone.utils.SparseWordIntVec;
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
    protected int nSampleWalks;
    protected static Random rand = new Random();

    public DTRandomWalkPredictor(List<PaperAbstract> documents,
                                 List<PaperAbstract> trainingSet,
                                 List<PaperAbstract> testingSet,
                                 Indexer<String> wordIndexer,
                                 Term[] terms,
                                 int walkLength,  
                                 int nSampleWalks) {
        super("DTRandomWalkPredictor");
        this.documents = documents;
        this.trainingSet = trainingSet;
        this.testingSet = testingSet;
        this.wordIndexer = wordIndexer;
        this.terms = terms;
        this.walkLength = walkLength;
        this.nSampleWalks = nSampleWalks;
    }

    public Integer[][] predict(int k, boolean outputUsedWord, File outputDirectory) {
	PlusoneFileWriter writer = makePredictionWriter(k, outputUsedWord, outputDirectory, null);
        Integer[][] ret = new Integer[testingSet.size()][];
	for (int document = 0; document < testingSet.size(); 
	     document ++) {
	    PaperAbstract a = testingSet.get(document);

            /* Add together words at the end of nSampleWalks random walks. */
            SparseWordIntVec v = new SparseWordIntVec();
            for (int i = 0; i < nSampleWalks; ++ i) {
                PaperAbstract endOfWalk = walk(a);
                if (null == endOfWalk) continue;
                v.plusEquals(new SparseWordIntVec(endOfWalk));
            }

            ret[document] = v.topKExcluding(k, outputUsedWord ? null : a);
	    for (int i = 0; i < ret[document].length; ++ i) {
                writer.write(wordIndexer.get(ret[document][i]) + " " );
	    }
            writer.write("\n");
	}

        writer.close();
	return ret;
    }

    protected PaperAbstract walk(PaperAbstract start) {
        PaperAbstract abs = start;
        for (int i = 0; i < walkLength; ++i) {
            List<Integer> words = abs.outputWords;
            if (words.size() == 0)
                return null;
            Integer word = words.get(rand.nextInt(words.size()));
            List<PaperAbstract> wordDocs = terms[word].getDocTrain();
            if (wordDocs.size() == 0)
                return null;
            abs = wordDocs.get(rand.nextInt(wordDocs.size()));
        }
        return abs;
    }
}

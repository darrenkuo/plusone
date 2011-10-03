package plusone.clustering;

import java.io.File;
import java.util.List;
import java.util.Random;
import plusone.utils.Indexer;
import plusone.utils.PaperAbstract;
import plusone.utils.PlusoneFileWriter;
import plusone.utils.SparseIntIntVec;
import plusone.utils.Term;

public class KNNRandomWalkPredictor extends KNN {
    final int walkLength;
    final double pLazy;
    final int nSampleWalks;

    /* This graph will include self-loops, but the random walk will act as if
     * those self-loop edges didn't exist, instead using pLazy to decide
     * whether to move to a new node.
     */
    final protected  Integer[][] trainingSetKNNGraph;

    public KNNRandomWalkPredictor(int K_CLOSEST,
                               List<PaperAbstract> documents,
                               List<PaperAbstract> trainingSet,
                               List<PaperAbstract> testingSet,
                               Indexer<String> wordIndexer,
                               Term[] terms,
                               int walkLength,
                               double pLazy,
                               int nSampleWalks) {
        super(K_CLOSEST, documents, trainingSet,
              testingSet, wordIndexer, terms);
        this.testName = "KNNRandomWalkPredictor";
        this.walkLength = walkLength;
        this.pLazy = pLazy;
        this.nSampleWalks = nSampleWalks;
        trainingSetKNNGraph = new Integer[trainingSet.size()][];
        for (int i = 0; i < trainingSet.size(); ++ i) {
            trainingSetKNNGraph[i] = kNbr(trainingSet.get(i), K_CLOSEST);
        }
    }

    public Integer[][] predict(int k, boolean outputUsedWord, File outputDirectory) {
	Integer[][] ret = new Integer[this.testingSet.size()][];
	
	PlusoneFileWriter writer = makePredictionWriter(k, outputUsedWord, outputDirectory, Integer.toString(K_CLOSEST));
	
	for (int document = 0; document < testingSet.size(); 
	     document ++) {
	    PaperAbstract a = testingSet.get(document);

            /* Add together documents at the end of nSampleWalks random
             * walks. */
            SparseIntIntVec v = new SparseIntIntVec();
            for (int i = 0; i < nSampleWalks; ++ i)
                v.plusEquals(new SparseIntIntVec(trainingSet.get(walk(a))));

            ret[document] = v.topK(k);
	    for (int i = 0; i < ret[document].length; ++ i) {
                writer.write(wordIndexer.get(ret[document][i]) + " " );
	    }
            writer.write("\n");
	}

        writer.close();
	return ret;
    }

    /** Perform a random walk on the graph of training documents, using the
     *  walkLength and pLazy fields to determine the length of the walk and the
     *  probability of not moving, respectively.  See the comment about
     *  self-loops above the "trainingSetKNNGraph" field.  The first step of
     *  the walk is special: the starting document need not be a node in the
     *  graph; pLazy is ignored for this step (but if start is in the training
     *  set, then walk might stay at it); and it does not count toward
     *  walkLength.
     */
    protected int walk(PaperAbstract start) {
        Random rand = new Random();
        Integer[] startNeighbors = kNbr(start, K_CLOSEST);
        int p = startNeighbors[rand.nextInt(startNeighbors.length)];
        for (int i = 0; i < walkLength; ++ i) {
            if (rand.nextDouble() < pLazy) continue;
            p = trainingSetKNNGraph[p][rand.nextInt(trainingSetKNNGraph[p].length)];
        }
        return p;
    }
}


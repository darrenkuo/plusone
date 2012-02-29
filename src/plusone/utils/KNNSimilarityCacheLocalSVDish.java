package plusone.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * This class is made to precompute and cache similarity distance
 * between each testing paper and training paper and to store the
 * ordering/ranking of the training paper for testing paper to speed
 * up the KNN* algorithm.
 */
public class KNNSimilarityCacheLocalSVDish {

    private final Map<Integer, Integer[]> distanceRank =
	new HashMap<Integer, Integer[]>();
    private final List<TrainingPaper> trainingPapers;
    private final List<PredictionPaper> testingPapers;
    private final LocalSVDish svd;

    public KNNSimilarityCacheLocalSVDish(List<TrainingPaper> trainingPapers,
					 List<PredictionPaper> testingPapers,
					 LocalSVDish svd) {
	this.trainingPapers = trainingPapers;
	this.testingPapers = testingPapers;
	this.svd = svd;

	System.out.println("[SimilarityCache] filling cache.");
	long t1 = System.currentTimeMillis();
	calculateDistances();
	System.out.format("[SimilarityCache] took %.3f seconds.\n",
			  (System.currentTimeMillis() - t1) / 1000.0);
    }

    /**
     * Stores the ranking of the training paper for KNN.
     */
    private void calculateDistances() {
	for (PredictionPaper testPaper : testingPapers) {
	    distanceRank.put(((PaperAbstract)testPaper).index,
			     getTrainingRanks(testPaper));
	}
    }

    /**
     * Calculate the distances between each testing paper and each
     * training paper And returns the ranking of the training paper by
     * paper index.
     */
    private Integer[] getTrainingRanks(PredictionPaper testPaper) {
	Integer[] rank = new Integer[trainingPapers.size()];
	Queue<ItemAndScore> queue = new PriorityQueue<ItemAndScore>();
	
	for (TrainingPaper trainPaper : trainingPapers) {
	    double sim = svd.similarity(trainPaper.getIndex(), testPaper);
	    queue.offer(new ItemAndScore(((PaperAbstract)trainPaper).index, 
					 sim, false));
	}
	
	int index = 0;
	while (!queue.isEmpty()) {
	    rank[index++] = (Integer)queue.poll().item;
	}
	return rank;
    }

    public Integer[] getDistance(PredictionPaper testPaper) {
	Integer[] rank = distanceRank
	    .get(((PaperAbstract)testPaper).index);
	if (rank == null) {
	    rank = getTrainingRanks(testPaper);
	    distanceRank.put(((PaperAbstract)testPaper).index, rank);
	}
	return rank;
    }
}
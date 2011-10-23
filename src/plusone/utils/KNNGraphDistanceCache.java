package plusone.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import plusone.Main;

public class KNNGraphDistanceCache {
    
    public static class KeyPair {
	Integer key1, key2, value;
	public KeyPair(Integer key1, Integer key2, Integer value) {
	    this.key1 = key1;
	    this.key2 = key2;
	    this.value = value;
	}

	public boolean equals(Object obj) {
	    if (!(obj instanceof KeyPair)) {
		return false;
	    }
	    KeyPair kp = (KeyPair)obj;
	    return (kp.key1 == key1 && kp.key2 == key2);
	}
    }

    private int[] trainPaperKeys;
    private int[] testPaperKeys;
    private List<KeyPair>[] distances;
    private int[] testPaperNotFound;

    public final int BUCKETS = 10000;

    public KNNGraphDistanceCache(List<TrainingPaper> trainingPapers,
				 List<PredictionPaper> testingPapers) {
	trainPaperKeys = new int[trainingPapers.size()];
	testPaperKeys = new int[testingPapers.size()];
	testPaperNotFound = new int[testingPapers.size()];
	distances = new List[BUCKETS];

	long t1 = System.currentTimeMillis();
	System.out.println("[GraphDistanceCache] filling cache" );
	precompute(trainingPapers, testingPapers);
	System.out.format("[GraphDistanceCache] took %.3f seconds.\n",
			  (System.currentTimeMillis() - t1) / 1000.0); 
    }

    private void precompute(List<TrainingPaper> trainingPapers,
			    List<PredictionPaper> testingPapers) {
	for (PredictionPaper testpaper : testingPapers) {
	    precomputeSingleTestpaper(trainingPapers, testpaper);
	}
    }

    private void precomputeSingleTestpaper(List<TrainingPaper> trainPapers,
					   PredictionPaper testPaper) {
	Queue<Integer> currentQueue = new LinkedList<Integer>();
	Queue<Integer> nextQueue = new LinkedList<Integer>();
	Indexer<PaperAbstract> paperIndexer = Main.getPaperIndexer();
	Set<Integer> kSet = new HashSet<Integer>();
	Set<Integer> doneSet = new HashSet<Integer>();

	PaperAbstract b = (PaperAbstract) testPaper;
	for (Integer currentPaper : b.inReferences) {
	    currentQueue.offer(currentPaper);
	}

	for (Integer currentPaper : b.outReferences) {
	    currentQueue.offer(currentPaper);
	}

	for (TrainingPaper trainPaper : trainPapers) {
	    kSet.add(((PaperAbstract)trainPaper).index);
	}

	int d = 1, maxD = 1, 
	    testPaperIndex = b.index - trainPaperKeys.length;

	doneSet.add(b.index);
	
	while (!currentQueue.isEmpty()) {
	    for (Integer currentPaper : currentQueue) {
		if (doneSet.contains(currentPaper))
		    continue;

		if (kSet.contains(currentPaper))
		    add(currentPaper, testPaperIndex, d);

		maxD = Math.max(maxD, d);

		doneSet.add(currentPaper);
		 
		PaperAbstract paper = paperIndexer.get(currentPaper);
		for (Integer neighbors : paper.inReferences) {
		    nextQueue.offer(neighbors);
		}
		for (Integer neighbors : paper.outReferences) {
		    nextQueue.offer(neighbors);
		}
	    }
	    d ++;
	    currentQueue = nextQueue;
	    nextQueue = new LinkedList<Integer>();
	}

	kSet.removeAll(doneSet);
	testPaperNotFound[testPaperIndex] = maxD + 1;
    }

    private void add(int key1, int key2, int value) {
	
	while (trainPaperKeys[key1] == 0) {
	    trainPaperKeys[key1] = Main.getRandomGenerator().nextInt();
	}
	
	while (testPaperKeys[key2] == 0) {
	    testPaperKeys[key2] = Main.getRandomGenerator().nextInt();
	}

	int index = hashcode(trainPaperKeys[key1], 
			     testPaperKeys[key2]) % BUCKETS;
	if (distances[index] == null)
	    distances[index] = new ArrayList<KeyPair>();

	distances[index].add(new KeyPair(key1, key2, value));
    }

    public int get(int key1, int key2) {
	return getValue(key1, key2 - trainPaperKeys.length);
    }

    private int getValue(int key1, int key2) {
	List<KeyPair> lst = 
	    distances[hashcode(trainPaperKeys[key1], 
			       testPaperKeys[key2]) % BUCKETS];

	if (lst == null)
	    return -1;

	for (KeyPair kp : lst) {
	    if (kp.key1 == key1 && kp.key2 == key2)
		return kp.value;
	}

	return testPaperNotFound[key2];
    }

    private int hashcode(int key1, int key2) {
	return Math.abs(key1 ^ key2);
    }
}
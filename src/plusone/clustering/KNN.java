package plusone.clustering;

import plusone.utils.Document;
import plusone.utils.Indexer;
import plusone.utils.KBestList;
import plusone.utils.PaperAbstract;
import plusone.utils.PlusoneFileWriter;
import plusone.utils.Term;
import plusone.utils.WordAndScore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class KNN extends ClusteringTest {

    protected List<PaperAbstract> documents;
    protected List<PaperAbstract> trainingSet;
    protected List<PaperAbstract> testingSet;
    protected Indexer<String> wordIndexer;
    protected List<Document> model;
    protected Term[] terms;
    protected int K_CLOSEST;
    
    public KNN(int K_CLOSEST,
	       List<PaperAbstract> documents,
	       List<PaperAbstract> trainingSet,
	       List<PaperAbstract> testingSet,
	       Indexer<String> wordIndexer,
	       Term[] terms) {
	super("knn");
	this.K_CLOSEST = K_CLOSEST;
	this.documents = documents;
	this.trainingSet = trainingSet;
	this.testingSet = testingSet;
	this.wordIndexer = wordIndexer;
	this.terms = terms;	
    }
    
    public Integer[][] predict(int k, boolean outputUsedWord, File outputDirectory) {
	Integer[][] array = new Integer[this.testingSet.size()][];
	
	PlusoneFileWriter writer = null;
	if (outputDirectory != null) {
	    writer = new PlusoneFileWriter(new File(outputDirectory, 
						    this.testName + "-" + k + "-" + outputUsedWord + 
						    ".predict"));
	}
	
	for (int document = 0; document < this.testingSet.size(); 
	     document ++) {
	    PaperAbstract a = testingSet.get(document);
	    Integer[] kList = this.kNbr(a, this.K_CLOSEST);
	    
	    List<Integer> lst = predictTopKWordsWithKList(kList, a, k, 
							  outputUsedWord);

	    array[document] = new Integer[lst.size()];
	    for (int i = 0; i < lst.size(); i ++) {
		array[document][i] = lst.get(i);
		if (outputDirectory != null)
		    writer.write(this.wordIndexer.get(array[document][i]) + " " );
	    }
	    //array[document] = (Integer[])lst.toArray(new Integer[lst.size()]);

	    if (outputDirectory != null)
		writer.write("\n");
	}

	if (outputDirectory != null)
	    writer.close();
	return array;
    }
    
    protected List<Integer> predictTopKWordsWithKList(Integer[] kList,
						    PaperAbstract testDoc, 
						    int k, 
						    boolean outputUsedWords) {
	
	int[] count = new int[terms.length];
	List<Integer> wordSet = new ArrayList<Integer>();
	
	for (int i = 0; i < kList.length; i++){
	    PaperAbstract a = trainingSet.get(kList[i]);
	    Set<Map.Entry<Integer, Integer>> words = a.trainingTf.entrySet();
	    
	    Iterator<Map.Entry<Integer,Integer>> iterator = words.iterator();
	    
	    while (iterator.hasNext()){
		Map.Entry<Integer, Integer> entry = iterator.next();
		int key = entry.getKey();
		int cnt = entry.getValue();
		if (count[key] == 0)
		    wordSet.add(key);
		count[key]+=cnt;
	    }
	}
	
	PriorityQueue<WordAndScore> queue = 
	    new PriorityQueue<WordAndScore>(k + 1);
	for (int i = 0; i < wordSet.size(); i++) {
	    int wordId = wordSet.get(i);
	    if (!outputUsedWords && testDoc.getTf0(wordId) > 0)
		continue;
	    if (queue.size() < k || 
		(double)count[wordId] > queue.peek().score){
		if (queue.size() >= k)
		    queue.poll();
		queue.add(new WordAndScore(wordId, 
					   (double)count[wordId], true));
	    }
	}
	
	List<Integer> results = new ArrayList<Integer>(Math.min(k, queue.size()));
	for (int i = 0; i < k && !queue.isEmpty(); i ++) {
	    results.add(queue.poll().wordID);
	}
	
	return results;
    }	
    
    
    public Integer[] kNbr(PaperAbstract doc, int K_CLOSEST){
	PriorityQueue<WordAndScore> queue = 
	    new PriorityQueue<WordAndScore>(K_CLOSEST + 1);
	
	for (int i = 0; i < trainingSet.size(); i++) {
	    PaperAbstract a = trainingSet.get(i);
	    double sim = doc.similarity(a)/a.getLength();
	    
	    if (queue.size() < K_CLOSEST || sim > queue.peek().score) {
		if (queue.size() >= K_CLOSEST) {
		    queue.poll();
		}
		queue.add(new WordAndScore(i, sim, true));
	    }
	}
	
	Integer[] results = new Integer[Math.min(K_CLOSEST, queue.size())];
	for (int i = 0; i < K_CLOSEST && !queue.isEmpty(); i ++) {
	    results[i] = queue.poll().wordID;
	}
	return results;
    }
}
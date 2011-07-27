package plusone.utils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Document {

    class WordCount {
	Integer wordID;
	int count;
	public WordCount(Integer wordID, int count) {
	    this.wordID = wordID;
	    this.count = count;
	}
    }
    
    protected Map<Integer, Integer> words;

    public Document() {
	this.words = new HashMap<Integer, Integer>();
    }

    public void addWord(Integer wordID) {
	if (this.words.containsKey(wordID))
	    this.words.put(wordID, this.words.get(wordID) + 1);
	else
	    this.words.put(wordID, 1);
    }

    public void addWord(Integer wordID, int count) {
	if (this.words.containsKey(wordID))
	    this.words.put(wordID, this.words.get(wordID) + count);
	else
	    this.words.put(wordID, count);
    }

    public Iterator<Integer> getWordIterator() {
	return this.words.keySet().iterator();
    }

    public int size() {
	return this.words.size();
    }

    public Integer getWordCount(Integer wordID) {
	if (this.words.containsKey(wordID))
	    return this.words.get(wordID);
	return 0;
    }

    public double distance(Document d) {
	double dist = 0.0, dist1 = 0.0, dist2 = 0.0;
	Iterator<Integer> myKeysIter = this.getWordIterator();
	while (myKeysIter.hasNext()) {
	    Integer key = myKeysIter.next();
	    dist1 += Math.pow(this.getWordCount(key), 2.0);
	}
	dist1 = Math.pow(dist1, 0.5);

	Iterator<Integer> dKeysIter = d.getWordIterator();
	while (dKeysIter.hasNext()) {
	    Integer key = dKeysIter.next();
	    dist2 += Math.pow(d.getWordCount(key), 2.0);
	}
	dist2 = Math.pow(dist2, 0.5);

	Set commonKeys = this.words.keySet();
	commonKeys.retainAll(d.words.keySet());	

	Iterator<Integer> cKeysIter = commonKeys.iterator();
	while (cKeysIter.hasNext()) {
	    Integer cKey = cKeysIter.next();
	    dist += this.getWordCount(cKey) * d.getWordCount(cKey);
	}
	dist /= (dist1 * dist2);

	/*
	Set commonKeys = this.words.keySet();
	commonKeys.retainAll(d.words.keySet());

	Iterator<Integer> myKeysIter = this.getWordIterator();
	while (myKeysIter.hasNext()) {
	    Integer key = myKeysIter.next();
	    if (!commonKeys.contains(key))
		dist += Math.pow(this.getWordCount(key), 2.0);
	}

	Iterator<Integer> dKeysIter = d.getWordIterator();
	while (dKeysIter.hasNext()) {
	    Integer key = dKeysIter.next();
	    if (!commonKeys.contains(key))
		dist += Math.pow(d.getWordCount(key), 2.0);
	}

	Iterator<Integer> cKeysIter = commonKeys.iterator();
	while (cKeysIter.hasNext()) {
	    Integer cKey = cKeysIter.next();
	    dist += Math.pow(this.getWordCount(cKey) - 
			     d.getWordCount(cKey), 2.0);	    
			     }
	*/
	
	return dist;	
    }

    public static Document abstractToDocument(PaperAbstract a) {
	Document d = new Document();
	for (Integer w : a.outputWords) {
	    d.addWord(w);
	}
	return d;
    }

    public static Comparator<Document> getComparator(final Integer axis) {
	return new Comparator<Document>() {
	    public int compare(Document d1, Document d2) {
		Integer c1 = d1.getWordCount(axis);
		Integer c2 = d2.getWordCount(axis);

		c1 = c1 == null ? 0 : c1;
		c2 = c2 == null ? 0 : c2;
		
		if (c1 < c2)
		    return -1;
		else if (c2 > c1)
		    return 1;
		return 0;
	    }

	    public boolean equals(Object obj) {
		// don't really need this now
		return false;
	    }
	};
    }
}
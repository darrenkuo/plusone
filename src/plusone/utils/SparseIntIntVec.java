package plusone.utils;

import java.lang.Iterable;
import java.lang.Math;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import plusone.utils.PaperAbstract;

public class SparseIntIntVec {
    Map<Integer, Integer> coords;

    public SparseIntIntVec() {
        coords = new HashMap<Integer, Integer>();
    }

    public SparseIntIntVec(PaperAbstract doc) {
        coords = new HashMap<Integer, Integer>(doc.trainingTf);
    }

    public void plusEquals(SparseIntIntVec v) {
        for (Map.Entry<Integer, Integer> entry : v.coords.entrySet()) {
            int key = entry.getKey();
            if (coords.containsKey(key))
                coords.put(key, coords.get(key) + entry.getValue());
            else
                coords.put(key, entry.getValue());
        }
    }

    public void dotEquals(Integer x) {
	for (Integer key : coords.keySet()) {
	    coords.put(key, coords.get(key) * x);
	}
    }

    public void addSingle(Integer coord, Integer value) {
	Integer oldValue = coords.get(coord);
	coords.put(coord, oldValue == null ? value : oldValue + value);
    }

    public Integer[] topKExcluding(int k, PaperAbstract excl) {
        PriorityQueue<WordAndScore> q = new PriorityQueue<WordAndScore>();
        for (Map.Entry<Integer, Integer> entry : coords.entrySet())
            if (excl == null || excl.getTf0(entry.getKey()) == 0)
                q.add(new WordAndScore(entry.getKey(), entry.getValue(), false /*XXX should be true*/));
        int kk = Math.min(k, q.size());
        Integer[] ret = new Integer[kk];
        for (int i = 0; i < kk; ++ i)
            ret[i] = q.poll().wordID;
        return ret;
    }

    public Integer[] topK(int k) { return topKExcluding(k, null); }

    public Iterable<Map.Entry<Integer, Integer>> pairs() {
	return coords.entrySet();
    }
}

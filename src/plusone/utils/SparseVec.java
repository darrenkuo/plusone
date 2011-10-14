package plusone.utils;

import java.lang.Iterable;
import java.lang.Math;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import plusone.utils.PaperAbstract;

public class SparseVec {
    Map<Integer, Double> coords;

    public SparseVec() {
        coords = new HashMap<Integer, Double>();
    }

    public SparseVec(HashMap<Integer, ? extends Number> freqs) {
        this();
        for (Map.Entry<Integer, ? extends Number> entry : freqs.entrySet())
            coords.put(entry.getKey(), entry.getValue().doubleValue());
    }

    public void plusEquals(SparseVec v) {
        for (Map.Entry<Integer, Double> entry : v.coords.entrySet()) {
            int key = entry.getKey();
            if (coords.containsKey(key))
                coords.put(key, coords.get(key) + entry.getValue());
            else
                coords.put(key, entry.getValue());
        }
    }

    public void dotEquals(Double x) {
	for (Integer key : coords.keySet()) {
	    coords.put(key, coords.get(key) * x);
	}
    }

    public void addSingle(Integer coord, Double value) {
	Double oldValue = coords.get(coord);
	coords.put(coord, oldValue == null ? value : oldValue + value);
    }

    public Integer[] topKExcluding(int k, PaperAbstract excl) {
        PriorityQueue<WordAndScore> q = new PriorityQueue<WordAndScore>();
        for (Map.Entry<Integer, Double> entry : coords.entrySet())
            if (excl == null || excl.getModelTf(entry.getKey()) == 0)
                q.add(new WordAndScore(entry.getKey(), entry.getValue(), false));
        int kk = Math.min(k, q.size());
        Integer[] ret = new Integer[kk];
        for (int i = 0; i < kk; ++ i)
            ret[i] = q.poll().wordID;
        return ret;
    }

    public Integer[] topK(int k) { return topKExcluding(k, null); }

    public Iterable<Map.Entry<Integer, Double>> pairs() {
	return coords.entrySet();
    }
    
    public final int cSize() {
	return coords.size();
    }
}

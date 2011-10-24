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
    
    /**
     * Makes a vector of the training word frequencies from the given paper.
     * @param paper
     */
    public SparseVec(PaperIF paper) {
	this();
	for (Integer word : paper.getTrainingWords())
	    coords.put(word, (double)paper.getTrainingTf(word));
    }

    public void plusEqualsWithCoef(SparseVec v, double c) {
	if (null == v) throw new NullPointerException();
        for (Map.Entry<Integer, Double> entry : v.coords.entrySet()) {
            int key = entry.getKey();
            if (coords.containsKey(key))
                coords.put(key, coords.get(key) + c * entry.getValue());
            else
                coords.put(key, c * entry.getValue());
        }
    }

    public void plusEquals(SparseVec v) {
	plusEqualsWithCoef(v, 1.0);
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
        PriorityQueue<ItemAndScore> q = new PriorityQueue<ItemAndScore>();
        for (Map.Entry<Integer, Double> entry : coords.entrySet())
            if (excl == null || excl.getTrainingTf(entry.getKey()) == 0)
                q.add(new ItemAndScore(entry.getKey(), entry.getValue(), false));
        int kk = Math.min(k, q.size());
        Integer[] ret = new Integer[kk];
        for (int i = 0; i < kk; ++ i)
            ret[i] = ((Integer)q.poll().item);
        return ret;
    }

    public Integer[] topK(int k) { return topKExcluding(k, null); }
    
    public Integer[] descending() {
	return topK(coords.size());
    }

    public Iterable<Map.Entry<Integer, Double>> pairs() {
	return coords.entrySet();
    }
    
    public int cSize() {
	return coords.size();
    }
    
    public double coordSum() {
	double ret = 0.0;
	for (Map.Entry<Integer, Double> entry : coords.entrySet())
	    ret += entry.getValue();
	return ret;
    }
}

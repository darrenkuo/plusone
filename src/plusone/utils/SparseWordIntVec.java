package plusone.utils;

import java.lang.Math;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import plusone.utils.PaperAbstract;

public class SparseWordIntVec {
    Map<Integer, Integer> coords;

    public SparseWordIntVec() {
        coords = new HashMap<Integer, Integer>();
    }

    public SparseWordIntVec(PaperAbstract doc) {
        coords = new HashMap<Integer, Integer>(doc.trainingTf);
    }

    public void plusEquals(SparseWordIntVec v) {
        for (Map.Entry<Integer, Integer> entry : v.coords.entrySet()) {
            int key = entry.getKey();
            if (coords.containsKey(key))
                coords.put(key, coords.get(key) + entry.getValue());
            else
                coords.put(key, entry.getValue());
        }
    }

    public Integer[] topK(int k) {
        PriorityQueue<WordAndScore> q = new PriorityQueue<WordAndScore>();
        for (Map.Entry<Integer, Integer> entry : coords.entrySet())
            q.add(new WordAndScore(entry.getKey(), entry.getValue(), true));
        int kk = Math.min(k, q.size());
        Integer[] ret = new Integer[kk];
        for (int i = 0; i < kk; ++ i)
            ret[i] = q.poll().wordID;
        return ret;
    }
}

package plusone.utils;

import java.lang.Integer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SampleDist<Outcome> {
    final static class CdfEntry<Outcome> {
	public final double p; /* The probability of drawing an outcome that comes before this one. */
	public final Outcome o; /* The outcome drawn. */
	public CdfEntry(double p, Outcome o) {
	    this.p = p;
	    this.o = o;
	}
    }
    
    protected final CdfEntry<Outcome>[] cdf;
    
    @SuppressWarnings("unchecked")
    protected SampleDist(List<CdfEntry<Outcome>> cdf) {
	this.cdf = (CdfEntry<Outcome>[]) cdf.toArray();
    }

    /**
     * @param v Should have no negative entries.
     */
    public static SampleDist<Integer> sampleVecCoords(SparseVec v) {
	List<CdfEntry<Integer>> cdf = new ArrayList<CdfEntry<Integer>>();
	double sum = v.coordSum();
	double cumProb = 0.0;
	for (Map.Entry<Integer, Double> pair : v.pairs()) {
	    cdf.add(new CdfEntry<Integer>(cumProb, pair.getKey()));
	    cumProb += pair.getValue() / sum;
	}
	return new SampleDist<Integer>(cdf);
    }
    
    public static SampleDist<Integer> samplePaperWords(PaperIF paper) {
	return sampleVecCoords(new SparseVec(paper));
    }
    
    public Outcome sample(Random r) {
	double x = r.nextDouble();
	// Return the first outcome whose cumulative probability is <= x.
	int a = 0, b = cdf.length;
	// Invariant: a <= target index < b
	while (a < b) {
	    int c = (a + b) / 2;
	    if (x >= cdf[c].p)
		a = c;
	    else
		b = c;
	}
	return cdf[a].o;
    }
}

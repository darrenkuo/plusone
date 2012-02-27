package plusone.clustering;

import plusone.Main;
import plusone.utils.ItemAndScore;
import plusone.utils.PredictionPaper;
import plusone.utils.SVD;
import plusone.utils.Terms;
import plusone.utils.TrainingPaper;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class LSI extends ClusteringTest {

    class Entry{
public int docID;
public int termID;
public double value;


public Entry(int docID, int termID, double value) {
this.docID = docID;
this.termID = termID;
this.value = value;
}
    }
    protected List<TrainingPaper> trainingSet;
    protected LinkedList<Entry>[] DocTerm;
    protected LinkedList<Entry>[] TermDoc;
    protected int DIMENSION;
    protected double[][] mu;
    protected double[][] beta;
    protected double[] sigma;
    protected Terms terms;
    protected SVD svd;
    public int numTerms;

    public LSI(int DIMENSION, List<TrainingPaper> trainingSet, Terms terms) {
super("LSI-" + DIMENSION);
this.DIMENSION = DIMENSION;
numTerms=terms.size();

svd = new SVD(DIMENSION, trainingSet, numTerms);
    }

    public Integer[] predict(int k, PredictionPaper testPaper) {
	return svd.predict(k,testPaper);
    }
}
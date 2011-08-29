package plusone.clustering;

import plusone.utils.Document;
import plusone.utils.Indexer;
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
import java.util.HashMap;
import java.util.LinkedList;

public class LSI extends ClusteringTest {
	class Entry{
		public int docID;
		public int termID;
		public double value;
		
		public Entry(int docID, int termID, double y){
			this.docID=docID;
			this.termID=termID;
			value=y;
		}
	}
    protected List<PaperAbstract> documents;
    protected List<PaperAbstract> trainingSet;
    protected List<PaperAbstract> testingSet;
    protected Indexer<String> wordIndexer;
    protected LinkedList<Entry>[] DocTerm;
    protected LinkedList<Entry>[] TermDoc;
    protected Term[] terms;
    protected int DIMENSION;
    protected double[][] mu;
    protected double[][] beta;
    protected double[] sigma;
    
    public LSI(int DIMENSION,
	       List<PaperAbstract> documents,
	       List<PaperAbstract> trainingSet,
	       List<PaperAbstract> testingSet,
	       Indexer<String> wordIndexer,
	       Term[] terms) {
	super("LSI");
	this.DIMENSION = DIMENSION;
	this.documents = documents;
	this.trainingSet = trainingSet;
	this.testingSet = testingSet;
	this.wordIndexer = wordIndexer;
	this.terms = terms;
	mu=new double[DIMENSION][documents.size()];
	beta=new double[DIMENSION][terms.length];
	for (int i=0;i<documents.size();i++){
		PaperAbstract doc=documents.get(i);
		DocTerm[i]= new LinkedList<Entry>();
		Set<Map.Entry<Integer, Integer>> words = doc.trainingTf.entrySet();
	    
	    Iterator<Map.Entry<Integer,Integer>> iterator = words.iterator();
	    
	    while (iterator.hasNext()){
		Map.Entry<Integer, Integer> entry = iterator.next();
		int key = entry.getKey();
		int cnt = entry.getValue();
		Entry temp=new Entry(i,key,cnt);
		DocTerm[i].add(temp);
		if (TermDoc[key]==null){
			TermDoc[key]=new LinkedList<Entry>();
		}
		TermDoc[key].add(temp);
	    }
	}
    }
    public double evalDiff(double[] x, double[] y){
    	double result=0;
    	for (int i=0;i<DocTerm.length;i++)
    		for (Entry t:DocTerm[i]){
    			result+=Math.pow(t.value-x[t.docID]*y[t.termID],2);
    		}
    	return result;
    }
    
    public void Iterate(double[] x,double[] y){
    	for (int i=0;i<x.length;i++)
    		x[i]=1.0;
    	for (int j=0;j<y.length;j++)
    		y[j]=1.0;
    	
    	double xnorm=x.length;
    	double ynorm=y.length;
    	double diff = evalDiff(x,y);
    	int rounds=0;
    	boolean converge = false;
    	while (!converge){
    		xnorm=0;
    		for (int i=0;i<x.length;i++){
    			double value=0;
    			for (Entry t:DocTerm[i]){
    				value+=t.value*y[t.termID];
    			}
    			xnorm+=value*value;
    			x[i]=value/ynorm;
    		}
    		ynorm=0;
    		for (int j=0;j<y.length;j++){
    			double value =0;
    			for (Entry t:TermDoc[j]){
    				value+=t.value*x[t.docID];
    			}
    			ynorm+=value*value;
    			y[j]=value/xnorm;
    		}
    		rounds++;
    		double temp=this.evalDiff(x, y);
    		if (Math.abs(diff-temp)<.0001)
    			converge=true;
    		diff=temp;
    	}
    	System.out.println(rounds);
    }
    
    public void subtract(double[] x, double[] y){
    	for (int i=0;i<DocTerm.length;i++)
    		for (Entry t:DocTerm[i]){
    			t.value-=x[t.docID]*y[t.termID];
    		}
    }
    public void orthog(double[] x1,double[] y1,double[] x2, double[] y2){
    	double length=0;
    	for (int i=0;i<x1.length;i++)
    		length+=x1[i]*x2[i];
    	for (int i=0;i<x2.length;i++)
    		x2[i]-=length*x1[i];
    	length=0;
    	for (int i=0;i<y1.length;i++)
    		length+=y1[i]*y2[i];
    	for (int i=0;i<y2.length;i++)
    		y2[i]-=length*y1[i];
    }
    public double normalize(double[] x,double[] y){
    	double lengthx=0;
    	for (int i=0;i<x.length;i++)
    		lengthx+=x[i]*x[i];
    	lengthx=Math.sqrt(lengthx);
    	for (int i=0;i<x.length;i++)
    		x[i]/=lengthx;
    	double lengthy=0;
    	for (int i=0;i<y.length;i++)
    		lengthy+=y[i]*y[i];
    	lengthy=Math.sqrt(lengthy);
    	for (int i=0;i<y.length;i++)
    		y[i]/=lengthy;
    	return lengthx*lengthy;
    }
    public void train(){
    	for (int k=0;k<DIMENSION;k++){
    		Iterate(mu[k],beta[k]);
    		subtract(mu[k],beta[k]);
    		for (int i=0;i<k;i++)
    			orthog(mu[i],beta[i],mu[k],beta[k]);
    		sigma[k]=normalize(mu[k],beta[k]); 		
    	}
    }

    	
    private double similarity(int docId, int termId){
    	double result=0;
    	for (int i=0;i<DIMENSION;i++)
    		result+=mu[i][docId]*sigma[i]*beta[i][termId];
    	return result;
    }
	private Integer[] predict(int k, boolean outputUsedWord,int Id, File outputDirectory){
		PriorityQueue<WordAndScore> queue = 
				new PriorityQueue<WordAndScore>(k+1);
	    for (int j = 0; j<terms.length;j++) {
		if (!outputUsedWord && documents.get(Id).getTf0(j) > 0)
	    		continue;
			double score = similarity(Id,j);
	    	if (queue.size() < k || 
		    score > queue.peek().score){
		    if (queue.size()>=k)
			queue.poll();
		    queue.add(new WordAndScore(j, 
					       score, true));
	    	}
	    }

	    Integer[] results = new Integer[Math.min(k, queue.size())];
	    for (int i = 0; i < k && !queue.isEmpty(); i ++) {
		Integer wordID = queue.poll().wordID;
		results[i] = wordID; 
	    }

	return results;
		
	}
	public Integer[][] predict(int k, boolean outputUsedWord,
			File outputDirectory) {
		this.train();
		Integer[][] result = new Integer[this.testingSet.size()][];
		for (int i=trainingSet.size();i<documents.size();i++){
			result[i]=predict(k,outputUsedWord,i,outputDirectory);
		}
		return result;
	}
}
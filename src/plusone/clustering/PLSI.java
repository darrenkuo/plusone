package plusone.clustering;

import plusone.utils.Indexer;
import plusone.utils.PaperAbstract;
import plusone.utils.PlusoneFileWriter;
import plusone.utils.PredictionPaper;
import plusone.utils.Terms;
import plusone.utils.ItemAndScore;
//import plusone.utils.MathVector;
import plusone.utils.TrainingPaper;

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
import java.io.*;

public class PLSI extends ClusteringTest{
	class PLSIEntry{
		public int docID;
		public int termID;
		public int tf;
		public double[] topics;

		public PLSIEntry(int docID, int termID,int tf){
			this.docID=docID;
			this.termID=termID;
			this.tf=tf;

		}
	}

	List<TrainingPaper> trainingSet;
	int vocabSize;
	int numTopics;
	protected LinkedList<PLSIEntry>[] DocTerm;
	//    protected LinkedList<PLSIEntry>[] TermDoc;
	protected double[][] termDistr;
	protected double[][] docDistr;
	protected double[] topicDistr;
	protected double beta=1.0;
	InputStreamReader converter = new InputStreamReader(System.in);
	BufferedReader in = new BufferedReader(converter);


	public PLSI(List<TrainingPaper> trainingDocuments, int vocabSize) {
		super("PLSI");
		this.trainingSet=trainingDocuments;
		this.vocabSize=vocabSize;
		DocTerm = new LinkedList[trainingSet.size()];


		for (int i=0;i<trainingSet.size();i++){
			TrainingPaper doc=trainingSet.get(i);
			DocTerm[i]= new LinkedList<PLSIEntry>();



			for (Integer word : doc.getTrainingWords()) {
				PLSIEntry temp=new PLSIEntry(i,word,doc.getTrainingTf(word));
				DocTerm[i].add(temp);
				/*			if (TermDoc[word]==null){
			    TermDoc[word]=new LinkedList<PLSIEntry>();
			}
			TermDoc[word].add(temp);*/
			}
		}
//		System.out.println("exiting constructor");

	}
	public void init(int numTopics){
		this.numTopics=numTopics;
		for (int i=0;i<trainingSet.size();i++){
			for (PLSIEntry x:DocTerm[i]){
				x.topics=new double[numTopics];
			}
		}
		termDistr=new double[numTopics][vocabSize];
		docDistr= new double[numTopics][trainingSet.size()];
		topicDistr= new double[numTopics];

		for (int i=0;i<numTopics;i++){
			for (int j=0;j<vocabSize;j++){
				termDistr[i][j]=Math.random()*100+1;
			}
			normalizeDistr(termDistr[i]);
			for (int j=0;j<trainingSet.size();j++){
				docDistr[i][j]=Math.random()*100+1;
			}
			normalizeDistr(docDistr[i]);
			topicDistr[i]=Math.random()*100+1;
		}
		normalizeDistr(topicDistr);
//		System.out.println("finishing init");
	}

	public void Estep(){
		//		System.out.println("E step start");
		for (int i=0;i<trainingSet.size();i++)
			for (PLSIEntry x:DocTerm[i]){
				for (int j=0;j<numTopics;j++){
					x.topics[j]=topicDistr[j]*docDistr[j][x.docID]*termDistr[j][x.termID];
				}
				normalizeDistr(x.topics);
			}
		//	System.out.println("Estep ends");
	}
	public void Mstep(){
		//	System.out.println("Mstep starts");
		termDistr=new double[numTopics][vocabSize];
		docDistr= new double[numTopics][trainingSet.size()];
		topicDistr= new double[numTopics];
		for (int i=0;i<trainingSet.size();i++)
			for (PLSIEntry x:DocTerm[i]){
				for (int j=0;j<numTopics;j++){
					termDistr[j][x.termID]+=x.tf*x.topics[j];
					docDistr[j][x.docID]+=x.tf*x.topics[j];
					topicDistr[j]+=x.tf*x.topics[j];
				}
			}
		for (int i=0;i<numTopics;i++){
			normalizeDistr(termDistr[i]);
			normalizeDistr(docDistr[i]);
		}
		normalizeDistr(topicDistr);
//		System.out.println("Mstep ends");
	}

	public double likelihood(){
		//System.out.println("evaluating likelihood");
		double l=0;
		for (int i=0;i<trainingSet.size();i++)
			for (PLSIEntry x:DocTerm[i]){
				double temp =0;
				for (int j=0;j<numTopics;j++){
					temp+=topicDistr[j]*termDistr[j][x.termID]*docDistr[j][x.docID];
				}
				l+=x.tf*Math.log(temp);
			}
//		System.out.print("likelihood=" + l);
		return l;
	}

	public void train(int numTopics){
		init(numTopics);
//		System.out.println("start training");
		double lkh = likelihood();
		boolean stop=false;
		while (!stop){
			Estep();
			Mstep();
			double newlkh= likelihood();
			if (Math.abs(lkh-newlkh)<.01)
				stop=true;
			lkh=newlkh;	
		} 
	}

	public void foldIn(LinkedList<PLSIEntry> testDoc, double[] distr){
		boolean stop=false;
		double[] distr_old;
		while (!stop){
			for (PLSIEntry x:testDoc){
				for (int j=0;j<numTopics;j++){
					x.topics[j]=distr[j]*termDistr[j][x.termID];
				}
				normalizeDistr(x.topics);
			}
/*			for (int i=0;i<distr.length;i++)
				System.out.print(distr[i]+" ");
			System.out.println();
*/
			distr_old=distr;
			distr= new double[numTopics];
			for (PLSIEntry x:testDoc){
				for (int j=0;j<numTopics;j++){
					distr[j]+=x.tf*x.topics[j];

				}
			}
			/*	try{
				in.readLine();
			}catch(Exception e) {
				e.printStackTrace();
		    }*/
			normalizeDistr(distr);
//			System.out.println("progress " + distance(distr,distr_old)+"\n");
			if (distance(distr, distr_old)<=0.001)
				stop=true;
			distr_old=distr;
		}
	}
	@Override
	public double[] predict(PredictionPaper testPaper) {
//		System.out.println("Enter predict");
		LinkedList<PLSIEntry> testDoc = new LinkedList<PLSIEntry>();
		for (Integer word:testPaper.getTrainingWords()){
			PLSIEntry temp=new PLSIEntry(-1,word,testPaper.getTrainingTf(word));
			temp.topics=new double[numTopics];
			testDoc.add(temp);
		}
//		System.out.println("finished parsing");
		double[] distr= new double[numTopics];
		for (int i=0;i<numTopics;i++)
			distr[i]=Math.random()*100+1;
		normalizeDistr(distr);
//		System.out.println("start folding");
		foldIn(testDoc,distr);
//		System.out.println("start computing posterior");

		double[] ret = new double[vocabSize];
		for (int i=0;i<vocabSize;i++) {				
			if (testPaper.getTrainingTf(i) > 0)
				continue;
			double score=0;
			for (int j=0;j<numTopics;j++)
				score+=termDistr[j][i]*distr[j];
			ret[i]= score;
			//System.out.println("PLSI PERPLEXITY " + getPerplexity(testPaper));
		}


		return ret;
	}
	private void normalizeDistr(double[] distr){
		double sum = 0;
		for (int i=0;i<distr.length;i++)
			sum+=distr[i];
		if (sum==0) return;
		for (int i=0;i<distr.length;i++)
			distr[i]=distr[i]/sum;
	}
	
	private double distance(double[] x, double[] y){
		if (x.length!=y.length){
			System.out.println("comparing distributions with different support!");
			return 0.0;
		}
		double dist=0;
		for (int i=0;i<x.length;i++)
			dist+=(x[i]-y[i])*(x[i]-y[i]);
		return Math.sqrt(dist);
	}
	
	private double getPerplexity(PredictionPaper testPaper) {
		double numerator = 0;
		for (int i=0;i<trainingSet.size();i++)
			for (PLSIEntry x:DocTerm[i]){
				double temp =0;
				for (int j=0;j<numTopics;j++){
					temp+=topicDistr[j]*termDistr[j][x.termID]*docDistr[j][x.docID];
				}
				numerator+=Math.log(temp);
			}
		
		double denominator = 0;
		for (int i=0; i<vocabSize; i++) {
			denominator += ((PaperAbstract)testPaper).getTestingTf(i);
		}
		return Math.exp(-1*numerator/denominator);
	}


}

package plusone.utils;

import java.util.List;
import java.util.ArrayList;

public class Results{
    private List<Double> predictionRate;
    private List<Double> tfScore;
    private List<Double> tfidfScore;
    private String expName;
    public Results(String expName){
	this.expName=expName;
	predictionRate= new ArrayList<Double>();	
	tfScore= new ArrayList<Double>();
	tfidfScore= new ArrayList<Double>();
    }
    public void addResult(double pred, double tf, double tfidf){
	predictionRate.add(pred);
	tfScore.add(tf);
	tfidfScore.add(tfidf);
    }
    private double mean(List<Double> list){
	double ret=0;
	for (double v:list)
	    ret+=v;
	ret=ret/list.size();
	return ret;
    }
    private double variance(List<Double> list){
	double avg = mean(list);
	double ret=0;
	for (double v:list)
	    ret +=(v-avg)*(v-avg);
	ret=ret/list.size();
	return ret;
    }

    public double[] getResultsMean(){
	double[] ret = new double[3];
	ret[0]=mean(predictionRate);
	ret[1]=mean(tfScore);
	ret[2]=mean(tfidfScore);
	return ret;
    }

    public double[] getResultsVariance(){
	double[] ret = new double[3];
	ret[0]=variance(predictionRate);
	ret[1]=variance(tfScore);
	ret[2]=variance(tfidfScore);
	return ret;
    }

}
